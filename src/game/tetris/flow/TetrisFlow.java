package game.tetris.flow;

import game.tetris.board.ActivePiece;
import game.tetris.board.Board;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.timer.timerClass;
import game.tetris.input.AI.PieceInfo;

public final class TetrisFlow
{
    private static final int LOCK_DELAY = 30;

    private static final int[] LINE_POINTS = {
            0, 100, 300, 500, 800
    };

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

    private final int startLevel;

    private int score;
    private int lines;
    private int level;

    private int gravityFrames;
    private int groundedFrames;

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
        menuOpen = !menuOpen;
    }

    public void togglePause()
    {
        if (started && !gameOver)
            paused = !paused;
    }

    public boolean isPlaying()
    {
        return started && !paused && !menuOpen &&
                !gameOver && activePiece.hasPiece();
    }

    public boolean isPaused() { return paused; }

    public boolean isMenuOpen() { return menuOpen; }

    public boolean isGameOver() { return gameOver; }


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