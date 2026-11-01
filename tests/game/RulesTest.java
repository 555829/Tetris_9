package game;

import game.tetris.board.ActivePiece;
import game.tetris.board.Board;
import game.tetris.flow.TetrisFlow;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.timer.timerClass;
import java.util.EnumSet;

/** Dependency-free regression tests. Run with run-tests.ps1. */
public final class RulesTest {
    private static int passed;

    public static void main(String[] args) {
        run("board dimensions and movement boundaries", RulesTest::boundaries);
        run("simultaneous line clearing and row compaction", RulesTest::lineClear);
        run("failed rotation preserves position and shape", RulesTest::rotation);
        run("garbage holes, rising rows and overflow", RulesTest::garbage);
        run("hold restriction and queue consumption", RulesTest::hold);
        run("seven bag and non-consuming queue preview", RulesTest::queue);
        run("lock delay ends on tick 30", RulesTest::lockDelay);
        run("grounded movement cannot delay locking forever", RulesTest::lockLimit);
        run("airborne movement does not consume lock resets", RulesTest::airborneMoves);
        run("paused/menu/game-over actions do not progress", RulesTest::stopped);
        run("soft and hard drop scoring", RulesTest::dropScore);
        run("single/double/triple/tetris attack table", RulesTest::attackTable);
        run("outgoing attacks accumulate and drain once", RulesTest::attackDrain);
        run("incoming attacks wait until lock", RulesTest::incoming);
        run("incoming overflow ends game before spawn", RulesTest::overflow);
        run("blocked spawn ends game", RulesTest::blockedSpawn);
        run("level changes after ten cleared lines", RulesTest::level);
        run("invalid attacks leave queue unchanged", RulesTest::invalidAttack);
        run("zero garbage is a no-op", RulesTest::zeroGarbage);
        run("legacy garbage API queues while paused", RulesTest::legacyGarbage);
        run("start level is honored and validated", RulesTest::startLevel);
        run("markGameOver discards incoming attacks", RulesTest::ended);
        run("two players exchange attacks exactly once", RulesTest::battle);
        run("paused versus games retain outgoing attacks", RulesTest::pausedBattle);
        run("kick and catch preserve pending attacks", RulesTest::kickCatch);
        run("kick timeout applies attacks after locking", RulesTest::kickTimeout);
        run("game over clears kicked state", RulesTest::kickedGameOver);
        run("morph keeps a legal active piece", RulesTest::morph);
        run("custom dimensions preserve Dev input and level setup", RulesTest::customSize);
        run("multiple incoming batches preserve hole positions", RulesTest::batches);
        System.out.println("PASS: " + passed + " rule tests");
    }

    private static void run(String name, Runnable test) {
        try { test.run(); }
        catch (Throwable failure) { throw new AssertionError("FAIL: " + name, failure); }
        passed++;
        System.out.println("PASS: " + name);
    }

    private static void check(boolean condition) {
        if (!condition) throw new AssertionError("Condition failed");
    }

    private static void invalid(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected IllegalArgumentException");
    }

    private static final class Fixture {
        final Board board;
        final ActivePiece piece;
        final TetrominoQueue queue;
        final TetrisFlow flow;
        Fixture(int width, int height, TetrominoEnum type) {
            board = new Board(width, height);
            piece = new ActivePiece(board);
            queue = new TetrominoQueue(() -> type);
            flow = new TetrisFlow(board, piece, queue, new timerClass(), 1, new java.util.Random(0) {
                @Override public double nextDouble() { return 1.0; }
            });
            flow.start();
        }
    }

    private static Fixture square() { return new Fixture(10, 20, TetrominoEnum.SquareShape); }

    private static int occupied(Board board) {
        int result = 0;
        for (int y = 0; y < board.getHeight(); y++)
            for (int x = 0; x < board.getWidth(); x++)
                if (board.getCell(x, y) != TetrominoEnum.NoShape) result++;
        return result;
    }

    private static void boundaries() {
        invalid(() -> new Board(2, 20));
        invalid(() -> new Board(10, 2));
        invalid(() -> new Board(21, 20));
        invalid(() -> new Board(10, 21));
        for (int width : new int[]{4, 10, 14}) {
            Fixture f = new Fixture(width, 20, TetrominoEnum.LineShape);
            while (f.piece.tryMoveLeft()) { }
            int x = f.piece.getX();
            check(!f.piece.tryMoveLeft() && f.piece.getX() == x);
            while (f.piece.tryMoveRight()) { }
            check(f.piece.getX() == width - 4);
            check(f.piece.hardDrop() == 18);
            check(!f.piece.tryOneLineDown());
        }
    }

    private static void lineClear() {
        Board board = new Board(4, 8);
        board.addGarbageLines(1, 1);
        board.addGarbageLines(1, 1);
        ActivePiece piece = new ActivePiece(board);
        TetrominoShape vertical = new TetrominoShape(TetrominoEnum.LineShape).rotateRight();
        check(piece.spawn(vertical, -1, 4));
        check(board.lockPieceAndClearAllLines(piece) == 2);
        check(occupied(board) == 2);
        check(board.getCell(1, 6) == TetrominoEnum.LineShape);
        check(board.getCell(1, 7) == TetrominoEnum.LineShape);
        for (int y = 0; y < 6; y++)
            for (int x = 0; x < 4; x++) check(board.getCell(x, y) == TetrominoEnum.NoShape);
    }

    private static void rotation() {
        Fixture f = new Fixture(10, 20, TetrominoEnum.LineShape);
        f.piece.hardDrop();
        TetrominoShape before = f.piece.getShape();
        int x = f.piece.getX(), y = f.piece.getY();
        check(!f.piece.tryRotateRight());
        check(f.piece.getShape() == before && f.piece.getX() == x && f.piece.getY() == y);
    }

    private static void garbage() {
        Board board = new Board(6, 4);
        check(board.addGarbageLines(1, 2));
        check(board.addGarbageLines(1, 4));
        check(board.getCell(2, 2) == TetrominoEnum.NoShape);
        check(board.getCell(4, 3) == TetrominoEnum.NoShape);
        check(occupied(board) == 10);
        check(board.addGarbageLines(1, 0));
        check(board.addGarbageLines(1, 0));
        check(!board.addGarbageLines(1, 0));
        int count = occupied(board);
        check(!board.addGarbageLines(1, 6));
        check(occupied(board) == count);
    }

    private static void hold() {
        final int[] index = {0};
        TetrominoEnum[] sequence = {TetrominoEnum.TShape, TetrominoEnum.LineShape,
                TetrominoEnum.SquareShape, TetrominoEnum.LShape};
        Board board = new Board(10, 20);
        ActivePiece piece = new ActivePiece(board);
        TetrominoQueue queue = new TetrominoQueue(() -> sequence[index[0]++ % sequence.length]);
        TetrisFlow flow = new TetrisFlow(board, piece, queue, new timerClass(), 1, new java.util.Random(0) {
                @Override public double nextDouble() { return 1.0; }
            });
        flow.start();
        check(flow.hold());
        check(flow.getHoldPiece() == TetrominoEnum.TShape);
        check(piece.getShape().getType() == TetrominoEnum.LineShape);
        check(!flow.hold());
        flow.hardDrop();
        TetrominoEnum next = queue.getQueue()[0];
        check(flow.hold());
        check(piece.getShape().getType() == TetrominoEnum.TShape);
        check(piece.getShape().getRotation() == game.tetris.tetromino.RotationState.SPAWN);
        check(queue.getQueue()[0] == next);
    }

    private static void queue() {
        PieceGenerator_7Bag bag = new PieceGenerator_7Bag();
        for (int batch = 0; batch < 100; batch++) {
            EnumSet<TetrominoEnum> seen = EnumSet.noneOf(TetrominoEnum.class);
            for (int i = 0; i < 7; i++) check(seen.add(bag.getPiece()));
            check(seen.size() == 7 && !seen.contains(TetrominoEnum.NoShape));
        }
        TetrominoQueue queue = new TetrominoQueue(new PieceGenerator_7Bag());
        for (int i = 0; i < 30; i++) {
            TetrominoEnum[] preview = queue.getQueue();
            check(preview.length == 5);
            TetrominoEnum first = preview[0];
            preview[0] = TetrominoEnum.NoShape;
            check(queue.getQueue()[0] == first);
            check(queue.getNextTetromino() == first);
        }
    }

    private static void lockDelay() {
        Fixture f = square();
        f.piece.hardDrop();
        for (int i = 0; i < 29; i++) check(!f.flow.tick());
        check(occupied(f.board) == 0);
        check(f.flow.tick() && occupied(f.board) == 4);
    }

    private static void lockLimit() {
        Fixture f = square();
        f.piece.hardDrop();
        boolean locked = false;
        for (int i = 0; i < 100 && !locked; i++) {
            check(f.flow.moveHorizontal(i % 2 == 0 ? -1 : 1));
            locked = f.flow.tick();
        }
        check(locked && occupied(f.board) == 4);
    }

    private static void airborneMoves() {
        Fixture f = square();
        for (int i = 0; i < 20; i++) check(f.flow.moveHorizontal(i % 2 == 0 ? -1 : 1));
        f.piece.hardDrop();
        for (int i = 0; i < 29; i++) check(!f.flow.tick());
        check(f.flow.moveHorizontal(-1));
        check(!f.flow.tick());
    }

    private static void stopped() {
        Fixture f = square();
        f.flow.togglePause();
        int y = f.piece.getY();
        for (int i = 0; i < 100; i++) check(!f.flow.tick());
        check(f.piece.getY() == y && !f.flow.hardDrop() && !f.flow.hold());
        f.flow.togglePause();
        f.flow.openMenu();
        check(!f.flow.softDrop() && !f.flow.moveHorizontal(1));
        f.flow.openMenu();
        check(f.flow.isPlaying());
    }

    private static void dropScore() {
        Fixture f = square();
        check(f.flow.softDrop() && f.flow.getScore() == 1);
        check(f.flow.hardDrop());
        check(f.flow.getScore() == 35);
    }

    private static Fixture clearRows(int rows) {
        Fixture f = new Fixture(4, 12, TetrominoEnum.LineShape);
        for (int i = 0; i < rows; i++) f.board.addGarbageLines(1, 2);
        check(f.flow.rotateRight());
        f.flow.hardDrop();
        check(f.flow.getLines() == rows);
        return f;
    }

    private static void attackTable() {
        int[] attacks = {0, 0, 1, 2, 4};
        for (int rows = 1; rows <= 4; rows++) {
            Fixture f = clearRows(rows);
            check(f.flow.consumeOutgoingAttack() == attacks[rows]);
        }
    }

    private static void attackDrain() {
        Fixture f = new Fixture(4, 20, TetrominoEnum.LineShape);
        for (int round = 0; round < 2; round++) {
            for (int i = 0; i < 4; i++) f.board.addGarbageLines(1, 2);
            check(f.flow.rotateRight());
            f.flow.hardDrop();
        }
        check(f.flow.consumeOutgoingAttack() == 8);
        check(f.flow.consumeOutgoingAttack() == 0);
    }

    private static void incoming() {
        Fixture f = square();
        f.flow.receiveAttack(2, 0);
        check(occupied(f.board) == 0 && f.flow.getPendingGarbageLines() == 2);
        f.flow.hold();
        check(f.flow.getPendingGarbageLines() == 2);
        f.flow.hardDrop();
        check(f.flow.getPendingGarbageLines() == 0);
        check(occupied(f.board) == 22);
        check(f.board.getCell(0, 19) == TetrominoEnum.NoShape);
        check(f.board.getCell(4, 16) == TetrominoEnum.SquareShape);
        check(f.flow.isPlaying());
    }

    private static void overflow() {
        Fixture f = square();
        f.flow.receiveAttack(21, 0);
        f.flow.hardDrop();
        check(f.flow.isGameOver() && !f.piece.hasPiece());
        check(!f.flow.hardDrop() && !f.flow.tick());
        f.flow.receiveAttack(2);
        check(f.flow.getPendingGarbageLines() == 0);
    }

    private static void blockedSpawn() {
        Fixture f = new Fixture(4, 4, TetrominoEnum.SquareShape);
        f.flow.hardDrop();
        check(!f.flow.isGameOver());
        f.flow.hardDrop();
        check(f.flow.isGameOver());
    }

    private static void level() {
        Fixture f = new Fixture(4, 20, TetrominoEnum.LineShape);
        for (int i = 0; i < 10; i++) f.flow.hardDrop();
        check(f.flow.getLines() == 10 && f.flow.getLevel() == 2);
        check(f.flow.getScore() == 1360);
        f.flow.hardDrop();
        check(f.flow.getScore() == 1596);
    }

    private static void invalidAttack() {
        Fixture f = square();
        invalid(() -> f.flow.receiveAttack(-1));
        invalid(() -> f.flow.receiveAttack(1, 10));
        invalid(() -> f.flow.receiveAttack(1, -1));
        f.flow.receiveAttack(0);
        check(f.flow.getPendingGarbageLines() == 0);
    }

    private static void zeroGarbage() {
        Board b = new Board(10, 20);
        check(b.addGarbageLines(0, 0) && occupied(b) == 0);
    }

    private static void legacyGarbage() {
        Fixture f = square();
        f.flow.togglePause();
        f.flow.applyGarbageLines(2, 1);
        check(f.flow.getPendingGarbageLines() == 2 && occupied(f.board) == 0);
        f.flow.togglePause();
        f.flow.hardDrop();
        check(f.flow.getPendingGarbageLines() == 0 && occupied(f.board) == 22);
    }

    private static void startLevel() {
        Fixture f = square();
        TetrisFlow flow = new TetrisFlow(f.board, f.piece, f.queue, new timerClass(), 5);
        check(flow.getLevel() == 5);
        invalid(() -> new TetrisFlow(f.board, f.piece, f.queue, new timerClass(), 0));
    }

    private static void ended() {
        Fixture f = square();
        f.flow.receiveAttack(2, 1);
        f.flow.markGameOver();
        check(f.flow.getPendingGarbageLines() == 0 && !f.piece.hasPiece());
        f.flow.receiveAttack(1, 0);
        check(f.flow.getPendingGarbageLines() == 0);
    }

    private static void battle() {
        Fixture first = clearRows(4), second = clearRows(2);
        game.tetris.flow.BattleRules.exchangeAttacks(first.flow, second.flow);
        check(first.flow.getPendingGarbageLines() == 1);
        check(second.flow.getPendingGarbageLines() == 4);
        game.tetris.flow.BattleRules.exchangeAttacks(first.flow, second.flow);
        check(first.flow.getPendingGarbageLines() == 1);
        check(second.flow.getPendingGarbageLines() == 4);
        first.flow.hardDrop();
        check(first.flow.getPendingGarbageLines() == 0 && !first.flow.isGameOver());
    }

    private static void pausedBattle() {
        Fixture first = clearRows(4), second = square();
        second.flow.togglePause();
        game.tetris.flow.BattleRules.exchangeAttacks(first.flow, second.flow);
        check(second.flow.getPendingGarbageLines() == 0);
        second.flow.togglePause();
        game.tetris.flow.BattleRules.exchangeAttacks(first.flow, second.flow);
        check(second.flow.getPendingGarbageLines() == 4);
    }

    private static void kickCatch() {
        Fixture f = square();
        check(f.flow.kick() && f.flow.isKicked() && !f.piece.hasPiece());
        f.flow.receiveAttack(2, 0);
        f.flow.togglePause();
        double ratio = f.flow.getKickedPiece().getRemainingRatio();
        for (int i = 0; i < 20; i++) check(!f.flow.tick());
        check(f.flow.getKickedPiece().getRemainingRatio() == ratio);
        check(!f.flow.catchKicked());
        f.flow.togglePause();
        check(f.flow.catchKicked() && f.piece.hasPiece());
        check(f.flow.getPendingGarbageLines() == 2 && occupied(f.board) == 0);
        f.flow.hardDrop();
        check(occupied(f.board) == 22 && f.flow.getPendingGarbageLines() == 0);
    }

    private static void kickTimeout() {
        Fixture f = square();
        check(f.flow.kick());
        f.flow.receiveAttack(1, 0);
        for (int i = 0; i < 119; i++) check(!f.flow.tick());
        check(f.flow.isKicked() && occupied(f.board) == 0);
        check(f.flow.tick());
        check(!f.flow.isKicked() && f.flow.getPendingGarbageLines() == 0);
        check(occupied(f.board) == 13 && f.flow.isPlaying());
    }

    private static void kickedGameOver() {
        Fixture f = square();
        check(f.flow.kick());
        f.flow.receiveAttack(1, 0);
        f.flow.markGameOver();
        check(!f.flow.isKicked() && !f.flow.catchKicked() && !f.flow.tick());
        check(f.flow.getPendingGarbageLines() == 0);
    }

    private static void morph() {
        Fixture f = square();
        check(f.flow.morph());
        check(f.piece.getShape().getType() != TetrominoEnum.SquareShape);
        check(f.flow.getMorphCount() == 1);
        check(f.board.canPlace(f.piece.getShape(), f.piece.getX(), f.piece.getY()));
    }

    private static void customSize() {
        game.tetris.Tetris tetrisGame = new game.tetris.Tetris(
                game.tetris.input.InputSourceType.Keyboard2,
                new TetrominoQueue(() -> TetrominoEnum.LineShape), 3, null, 8, 16);
        try {
            check(tetrisGame.board.getWidth() == 8 && tetrisGame.board.getHeight() == 16);
            check(tetrisGame.flow.getLevel() == 3 && tetrisGame.flow.isPlaying());
            check(tetrisGame.flow.hardDrop() && tetrisGame.flow.getScore() == 28);
        } finally { tetrisGame.close(); }
    }

    private static void batches() {
        Fixture f = square();
        f.flow.receiveAttack(1, 0);
        f.flow.receiveAttack(1, 9);
        f.flow.hardDrop();
        check(f.board.getCell(0, 18) == TetrominoEnum.NoShape);
        check(f.board.getCell(9, 19) == TetrominoEnum.NoShape);
        check(f.flow.getPendingGarbageLines() == 0);
    }
}
