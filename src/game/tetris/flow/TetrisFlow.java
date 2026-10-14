package game.tetris.flow;

import game.tetris.board.ActivePiece;
import game.tetris.board.Board;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.timer.timerClass;
import game.tetris.input.AI.PieceInfo;

import java.util.Random;

public final class TetrisFlow
{
    private static final int LOCK_DELAY = 30;

    private static final int[] LINE_POINTS = {
            0, 100, 300, 500, 800
    };

    // 블록 차기: 새 블록마다 KICK_CHANCE 확률로, 블록이 KICK_MIN_ROW ~ (KICK_MIN_ROW + KICK_ROW_RANGE - 1)줄까지
    private static final double KICK_CHANCE = 0.15;
    private static final int KICK_MIN_ROW = 3;
    private static final int KICK_ROW_RANGE = 5;
    private static final int KICK_FRAMES = 120;
    private static final double KICK_SPEED = 0.3;

    private final Board board;
    private final ActivePiece activePiece;
    private final TetrominoQueue queue;
    private final timerClass timer;

    private TetrominoEnum holdPiece = TetrominoEnum.NoShape;

    private boolean holdUsed;
    private boolean started;
    private boolean paused;
    private boolean menuOpen;
    private boolean gameOver;
    private boolean systemControlsEnabled = true;

    private final int startLevel;

    private int score;
    private int lines;
    private int level;

    private int gravityFrames;
    private int groundedFrames;

    private final Random random = new Random();
    private KickedPiece kicked;      // null이면 차인 블록 없음
    private int kickAtRow = -1;      // -1이면 이번 블록은 차이지 않음

    public TetrisFlow(
            Board board,
            ActivePiece activePiece,
            TetrominoQueue queue,
            timerClass timer,
            int startLevel)              //was: added
    {
        this.board = board;
        this.activePiece = activePiece;
        this.queue = queue;
        this.timer = timer;
        this.startLevel = startLevel;    //was: added
        this.level = startLevel;         //was: added
    }

    // 게임 최초 시작
    public void start()
    {
        if (started)
            return;

        started = true;
        spawnNextPiece();
    }





    // 현재는 메뉴 키로 열기/닫기를 함께 처리
    public void openMenu()
    {
        if (!systemControlsEnabled) return;
        menuOpen = !menuOpen;
    }

    public void togglePause()
    {
        if (systemControlsEnabled && started && !gameOver)
            paused = !paused;
    }

    public boolean isRunning()
    {
        return started && !paused && !menuOpen && !gameOver;
    }

    public boolean isPlaying()
    {
        return isRunning() && activePiece.hasPiece();
    }

    public boolean isKicked() { return kicked != null; }

    // 화면에서 그리기 위한 읽기 전용 정보. 없으면 null
    public KickedPiece getKickedPiece() { return kicked; }

    public boolean isPaused() { return paused; }

    public boolean isMenuOpen() { return menuOpen; }

    public boolean isGameOver() { return gameOver; }

    public boolean areSystemControlsEnabled() { return systemControlsEnabled; }

    public void setSystemControlsEnabled(boolean enabled) { systemControlsEnabled = enabled; }

    public void markGameOver()
    {
        gameOver = true;
        activePiece.clear();
    }


    // 좌우 이동 요청. direction은 -1 또는 1
    public boolean moveHorizontal(int direction)
    {
        if (!isPlaying() ||
                (direction != -1 && direction != 1))
            return false;

        boolean moved = activePiece.tryMove(direction, 0);

        if (moved)
            groundedFrames = 0;

        return moved;
    }


    public boolean rotateRight()
    {
        if (!isPlaying())
            return false;

        boolean rotated = activePiece.tryRotateRight();

        if (rotated)
            groundedFrames = 0;

        return rotated;
    }
    // 혹시 모르니
    public boolean rotateLeft()
    {
        if (!isPlaying())
            return false;

        boolean rotated = activePiece.tryRotateLeft();

        if (rotated)
            groundedFrames = 0;

        return rotated;
    }


    // 한 칸 낙하, 성공 시 1점
    public boolean softDrop()
    {
        if (!isPlaying())
            return false;

        boolean moved = activePiece.tryOneLineDown();

        if (moved)
        {
            score++;
            gravityFrames = 0;
            groundedFrames = 0;
        }

        return moved;
    }


    // 즉시 낙하 및 고정
    // true: 현재 Piece가 교체되었음을 뜻함, 이번 프레임 tick 정지
    public boolean hardDrop()
    {
        if (!isPlaying())
            return false;

        score += activePiece.hardDrop() * 2;

        lockCurrentPiece();

        return true;
    }


    // 현재 Piece와 HOLD Piece 교체
    // true: 현재 Piece가 교체되었음을 뜻함, 이번 프레임 tick 정지
    public boolean hold()
    {
        if (!isPlaying() || holdUsed)
            return false;

        TetrominoEnum previous =
                activePiece.getShape().getType();

        activePiece.clear();

        TetrominoEnum replacement;

        if (holdPiece == TetrominoEnum.NoShape)
        {
            holdPiece = previous;
            replacement = queue.getNextTetromino();
        }
        else
        {
            replacement = holdPiece;
            holdPiece = previous;
        }

        holdUsed = true;

        spawnPiece(replacement);

        return true;
    }


    // 매 프레임 자동 진행
    // true: 자연 낙하로 Piece가 고정 및 교체됨
    public boolean tick()
    {
        if (!isPlaying())
            return false;

        gravityFrames++;

        if (gravityFrames >= getGravityInterval())
        {
            gravityFrames = 0;
            activePiece.tryOneLineDown();
        }

        // 현재 Piece가 한 칸 더 내려갈 수 있는가?
        if (board.canPlace(
                activePiece.getShape(),
                activePiece.getX(),
                activePiece.getY() + 1))
        {
            groundedFrames = 0;
        }
        else
        {
            groundedFrames++;
        }

        if (groundedFrames >= LOCK_DELAY)
        {
            lockCurrentPiece();
            return true;
        }

        return false;
    }
    // 날아가는 블록을 잡음. 잡은 자리에서 다시 일반 낙하 시작
    public boolean catchKicked()
    {
        if (kicked == null || !isRunning())
            return false;

        placeKicked();
        return true;
    }

    private boolean tickKicked() {
        if (!isRunning())
            return false;

        if (kicked.tick(board.getWidth(), board.getHeight()))
            return false;

        if (placeKicked()) {
            activePiece.hardDrop();
            lockCurrentPiece();
        }
        return true;
    }

    private boolean placeKicked()
    {
        TetrominoShape shape = kicked.getShape();
        int x = (int) Math.round(kicked.getX());
        int y = (int) Math.round(kicked.getY());
        kicked = null;

        // 날아가는 동안 쌓인 블록을 통과하므로 겹친 위치일 수 있음 → 겹치지 않을 때까지 위로 올림
        while (y >= -TetrominoShape.SIZE && !board.canPlace(shape, x, y))
            y--;

        if (y < -TetrominoShape.SIZE || !activePiece.spawn(shape, x, y))
        {
            gameOver = true;
            return false;
        }

        gravityFrames = 0;
        groundedFrames = 0;
        return true;
    }



    // 현재 Piece를 Board에 고정하고 다음 Piece 생성
    private void lockCurrentPiece()
    {
        int cleared = board.lockPieceAndClearAllLines(activePiece);
        activePiece.clear();

        score += LINE_POINTS[Math.min(cleared, 4)] * level;

        lines += cleared;
        level = startLevel + lines / 10;     // was: level = 1 + lines / 10;

        holdUsed = false;

        spawnNextPiece();
    }


    private void spawnNextPiece()
    {
        spawnPiece(queue.getNextTetromino());
    }


    private void spawnPiece(TetrominoEnum type)
    {
        gravityFrames = 0;
        groundedFrames = 0;

        kickAtRow = random.nextDouble() < KICK_CHANCE
                ? KICK_MIN_ROW + random.nextInt(KICK_ROW_RANGE)
                : -1;

        TetrominoShape shape = new TetrominoShape(type);

        int spawnX =
                (board.getWidth() - TetrominoShape.SIZE) / 2;

        if (!activePiece.spawn(shape, spawnX, 0))
            gameOver = true;
    }


    // 레벨에 따른 자연 낙하 간격
    private int getGravityInterval()
    {
        return Math.max(5, 48 - (level - 1) * 5);
    }


    public int getScore() { return score; }

    public int getLines() { return lines; }

    public int getLevel() { return level; }

    public void applyGarbageLines(int lineCount, int holeColumn)
    {
        if (!isPlaying()) return;
        if (!board.addGarbageLines(lineCount, holeColumn))
        {
            gameOver = true;
            activePiece.clear();
            return;
        }

        while (activePiece.hasPiece()
                && !board.canPlace(activePiece.getShape(), activePiece.getX(), activePiece.getY()))
        {
            if (!activePiece.tryMove(0, -1))
            {
                gameOver = true;
                activePiece.clear();
                return;
            }
        }
        groundedFrames = 0;
    }

    public TetrominoEnum getHoldPiece()
    {
        return holdPiece == TetrominoEnum.NoShape
                ? queue.peekNextTetromino()
                : holdPiece;
    }

    public Board getBoard()
    {
        return board;
    }

    public PieceInfo getCurrentPieceInfo()
    {
        if (!activePiece.hasPiece())
            return null;

        return new PieceInfo(
                activePiece.getShape().getType(),
                activePiece.getShape().getRotation(),
                activePiece.getX(),
                activePiece.getY()
        );
    }
}