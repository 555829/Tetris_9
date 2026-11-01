package game.ui.multigame.engine.flow;

import game.ui.multigame.engine.board.ActivePiece;
import game.ui.multigame.engine.board.Board;
import game.ui.multigame.engine.board.SpawnPosition;
import game.ui.multigame.engine.input.AI.PieceInfo;
import game.ui.multigame.engine.tetromino.TetrominoEnum;
import game.ui.multigame.engine.tetromino.TetrominoShape;
import game.ui.multigame.engine.tetrominoQueue.TetrominoQueue;
import game.ui.multigame.engine.timer.timerClass;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Random;

public final class TetrisFlow
{
    private static final int LOCK_DELAY = 30;
    private static final int MAX_LOCK_RESETS = 15;
    private static final int[] LINE_ATTACKS = {0, 0, 1, 2, 4};

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
    private boolean systemControlsEnabled = true;

    private final int startLevel;

    private int score;
    private int lines;
    private int level;

    private int gravityFrames;
    private int groundedFrames;
    private int lockResets;
    private int outgoingAttack;
    private int pendingGarbageLines;
    // Each batch stores {line count, hole column}; memory does not grow per row.
    private final Deque<int[]> pendingGarbage = new ArrayDeque<>();
    private final Random garbageRandom = new Random();

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
        if (startLevel < 1) throw new IllegalArgumentException("Start level must be positive");
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

    public boolean isPaused() { return paused; }

    public boolean isMenuOpen() { return menuOpen; }

    public boolean isGameOver() { return gameOver; }

    public boolean areSystemControlsEnabled() { return systemControlsEnabled; }

    public void setSystemControlsEnabled(boolean enabled) { systemControlsEnabled = enabled; }

    public void markGameOver()
    {
        gameOver = true;
        activePiece.clear();
        pendingGarbage.clear();
        pendingGarbageLines = 0;
    }


    // 좌우 이동 요청. direction은 -1 또는 1
    public boolean moveHorizontal(int direction)
    {
        if (!isPlaying() ||
                (direction != -1 && direction != 1))
            return false;

        boolean wasGrounded = isGrounded();
        boolean moved = activePiece.tryMove(direction, 0);

        if (moved)
            resetLockDelay(wasGrounded);

        return moved;
    }


    public boolean rotateRight()
    {
        if (!isPlaying())
            return false;

        boolean wasGrounded = isGrounded();
        boolean rotated = activePiece.tryRotateRight();

        if (rotated)
            resetLockDelay(wasGrounded);

        return rotated;
    }
    // 혹시 모르니
    public boolean rotateLeft()
    {
        if (!isPlaying())
            return false;

        boolean wasGrounded = isGrounded();
        boolean rotated = activePiece.tryRotateLeft();

        if (rotated)
            resetLockDelay(wasGrounded);

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
        outgoingAttack += LINE_ATTACKS[Math.min(cleared, 4)];

        // Apply only after locking, so incoming rows cannot overlap an active piece.
        while (!pendingGarbage.isEmpty())
        {
            int[] batch = pendingGarbage.removeFirst();
            pendingGarbageLines -= batch[0];
            if (!board.addGarbageLines(batch[0], batch[1]))
            {
                markGameOver();
                return;
            }
        }
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
        lockResets = 0;

        SpawnPosition spawn = SpawnPosition.find(board.getWidth(), board.getHeight(), type);
        if (spawn == null || !activePiece.spawn(spawn.shape, spawn.x, spawn.y))
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

    private boolean isGrounded()
    {
        return !board.canPlace(activePiece.getShape(), activePiece.getX(), activePiece.getY() + 1);
    }

    private void resetLockDelay(boolean wasGrounded)
    {
        if (wasGrounded && lockResets < MAX_LOCK_RESETS)
        {
            groundedFrames = 0;
            lockResets++;
        }
    }

    /** Kept for existing callers; now queues rows until the current piece locks. */
    public void applyGarbageLines(int lineCount, int holeColumn)
    {
        receiveAttack(lineCount, holeColumn);
    }

    public void receiveAttack(int lineCount)
    {
        receiveAttack(lineCount, garbageRandom.nextInt(board.getWidth()));
    }

    public void receiveAttack(int lineCount, int holeColumn)
    {
        if (lineCount < 0 || holeColumn < 0 || holeColumn >= board.getWidth())
            throw new IllegalArgumentException("Invalid attack");
        if (gameOver || lineCount == 0) return;
        int total = Math.addExact(pendingGarbageLines, lineCount);
        pendingGarbage.addLast(new int[]{lineCount, holeColumn});
        pendingGarbageLines = total;
    }

    public int getPendingGarbageLines() { return pendingGarbageLines; }

    /** Drain once per game loop and forward to the opponent. */
    public int consumeOutgoingAttack()
    {
        int result = outgoingAttack;
        outgoingAttack = 0;
        return result;
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
