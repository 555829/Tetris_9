package game.tetris.board;

import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

/** Geometry-only spawn selection, shared by gameplay and AI Hold planning. */
public final class SpawnPosition {
    public final TetrominoShape shape;
    public final int x;
    public final int y;

    private SpawnPosition(TetrominoShape shape, int x, int y) {
        this.shape = shape;
        this.x = x;
        this.y = y;
    }

    public static SpawnPosition find(int width, int height, TetrominoEnum type) {
        TetrominoShape shape = new TetrominoShape(type);
        for (int rotation = 0; rotation < 4; rotation++, shape = shape.rotateRight()) {
            int minX = 4, maxX = -1, minY = 4, maxY = -1;
            for (int y = 0; y < TetrominoShape.SIZE; y++)
                for (int x = 0; x < TetrominoShape.SIZE; x++)
                    if (shape.get(x, y) != TetrominoEnum.NoShape) {
                        minX = Math.min(minX, x); maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y); maxY = Math.max(maxY, y);
                    }
            if (maxX < 0) return null;
            int shapeWidth = maxX - minX + 1, shapeHeight = maxY - minY + 1;
            if (shapeWidth > width || shapeHeight > height) continue;
            return new SpawnPosition(shape, (width - shapeWidth) / 2 - minX,
                    maxY < height ? 0 : -minY);
        }
        return null;
    }
}
