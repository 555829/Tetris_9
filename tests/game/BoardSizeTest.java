package game;

import game.tetris.Tetris;
import game.tetris.board.ActivePiece;
import game.tetris.board.Board;
import game.tetris.board.SpawnPosition;
import game.tetris.flow.TetrisFlow;
import game.tetris.input.AI.PieceInfo;
import game.tetris.input.AI.TetrisAI;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.timer.timerClass;
import java.util.EnumSet;
import java.util.Random;

/** Engine size limits, spawning, piece generation and AI compatibility. */
public final class BoardSizeTest {
    public static void main(String[] args) throws Exception {
        bounds();
        spawnEverySize();
        multiplayerSpawnEverySize();
        bags();
        ai();
        constructors();
        System.out.println("PASS: 6 board size tests (all 324 sizes in both engines)");
    }

    private static void check(boolean value) {
        if (!value) throw new AssertionError("Board size check failed");
    }

    private static void bounds() {
        for (int bad : new int[]{Integer.MIN_VALUE, -1, 0, 2, 21, Integer.MAX_VALUE}) {
            expectInvalid(() -> new Board(bad, 10));
            expectInvalid(() -> new Board(10, bad));
            expectInvalid(() -> new game.ui.multigame.engine.board.Board(bad, 10));
            expectInvalid(() -> new game.ui.multigame.engine.board.Board(10, bad));
        }
        System.out.println("PASS: both engines reject dimensions outside 3..20");
    }

    private static void expectInvalid(Runnable action) {
        try { action.run(); }
        catch (IllegalArgumentException expected) { return; }
        throw new AssertionError("Expected invalid board size");
    }

    private static void spawnEverySize() {
        for (int width = 3; width <= 20; width++)
            for (int height = 3; height <= 20; height++)
                for (TetrominoEnum type : TetrominoEnum.values()) {
                    if (type == TetrominoEnum.NoShape) continue;
                    Board board = new Board(width, height);
                    ActivePiece piece = new ActivePiece(board);
                    TetrisFlow flow = new TetrisFlow(board, piece,
                            new TetrominoQueue(() -> type), new timerClass(), 1, new Random(0));
                    flow.start();
                    if (width == 3 && height == 3 && type == TetrominoEnum.LineShape) {
                        check(flow.isGameOver());
                        continue;
                    }
                    check(flow.isPlaying() && board.canPlace(piece.getShape(), piece.getX(), piece.getY()));
                    flow.hardDrop(); // Includes line clearing on narrow/short boards.
                }
        System.out.println("PASS: every fitting piece spawns and drops on all 324 sizes");
    }

    private static void multiplayerSpawnEverySize() {
        for (int width = 3; width <= 20; width++)
            for (int height = 3; height <= 20; height++)
                for (game.ui.multigame.engine.tetromino.TetrominoEnum type
                        : game.ui.multigame.engine.tetromino.TetrominoEnum.values()) {
                    if (type == game.ui.multigame.engine.tetromino.TetrominoEnum.NoShape) continue;
                    game.ui.multigame.engine.board.Board board =
                            new game.ui.multigame.engine.board.Board(width, height);
                    game.ui.multigame.engine.board.ActivePiece piece =
                            new game.ui.multigame.engine.board.ActivePiece(board);
                    game.ui.multigame.engine.flow.TetrisFlow flow =
                            new game.ui.multigame.engine.flow.TetrisFlow(board, piece,
                            new game.ui.multigame.engine.tetrominoQueue.TetrominoQueue(() -> type),
                            new game.ui.multigame.engine.timer.timerClass(), 1);
                    flow.start();
                    if (width == 3 && height == 3
                            && type == game.ui.multigame.engine.tetromino.TetrominoEnum.LineShape) {
                        check(flow.isGameOver());
                        continue;
                    }
                    check(flow.isPlaying() && board.canPlace(piece.getShape(), piece.getX(), piece.getY()));
                    flow.hardDrop();
                }
        System.out.println("PASS: multiplayer engine supports the same 324 sizes");
    }

    private static void bags() {
        PieceGenerator_7Bag bag = new PieceGenerator_7Bag(3, 3);
        game.ui.multigame.engine.tetrominoQueue.PieceGenerator_7Bag multi =
                new game.ui.multigame.engine.tetrominoQueue.PieceGenerator_7Bag(3, 3);
        for (int batch = 0; batch < 100; batch++) {
            EnumSet<TetrominoEnum> seen = EnumSet.noneOf(TetrominoEnum.class);
            for (int i = 0; i < 6; i++) {
                TetrominoEnum type = bag.getPiece();
                check(type != TetrominoEnum.LineShape && seen.add(type));
                check(multi.getPiece() != game.ui.multigame.engine.tetromino.TetrominoEnum.LineShape);
            }
            check(seen.size() == 6);
        }
        System.out.println("PASS: 3x3 queue excludes only the impossible I piece");
    }

    private static void ai() {
        for (int[] size : new int[][]{{3, 6}, {6, 3}, {3, 3}}) {
            Board board = new Board(size[0], size[1]);
            SpawnPosition spawn = SpawnPosition.find(size[0], size[1], TetrominoEnum.TShape);
            PieceInfo info = new PieceInfo(TetrominoEnum.TShape, spawn.shape.getRotation(), spawn.x, spawn.y);
            check(new TetrisAI().getMoveList(board, info, TetrominoEnum.LineShape).length > 0);
        }
        System.out.println("PASS: AI plans moves with narrow/short board Hold spawn positions");
    }

    private static void constructors() {
        for (int[] size : new int[][]{{3, 3}, {3, 20}, {20, 3}, {20, 20}}) {
            Tetris single = new Tetris(game.tetris.input.InputSourceType.Keyboard2,
                    new TetrominoQueue(new PieceGenerator_7Bag(size[0], size[1])),
                    1, null, size[0], size[1]);
            try {
                check(single.board.getWidth() == size[0] && single.board.getHeight() == size[1]);
                check(single.flow.isPlaying());
            } finally { single.close(); }
            game.ui.multigame.engine.Tetris multi = new game.ui.multigame.engine.Tetris(
                    game.ui.multigame.engine.input.InputSourceType.Keyboard2,
                    new game.ui.multigame.engine.tetrominoQueue.TetrominoQueue(
                            new game.ui.multigame.engine.tetrominoQueue.PieceGenerator_7Bag(size[0], size[1])),
                    1, null, size[0], size[1]);
            try {
                check(multi.board.getWidth() == size[0] && multi.board.getHeight() == size[1]);
                check(multi.flow.isPlaying());
            } finally { multi.close(); }
        }
        System.out.println("PASS: both engine constructors apply dimensions without UI changes");
    }
}
