package game.tetris.board;

import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

import java.util.Arrays;

public final class ActivePiece
{
    private final int width;
    private final int height;

    private final TetrominoEnum[][] activeLayer;
    private final Board board;

    private TetrominoShape shape;

    private int x;
    private int y;

    public ActivePiece(Board board)
    {
        this.board = board;
        width = board.getWidth();
        height = board.getHeight();

        activeLayer = new TetrominoEnum[height][width];

        clear();
    }

    public boolean spawn(TetrominoShape newShape, int spawnX, int spawnY)
    {
        if (hasPiece())
            throw new IllegalStateException("Piece already active");

        if (newShape == null ||
                newShape.getType() == TetrominoEnum.NoShape)
            throw new IllegalArgumentException("Invalid shape");

        if (!board.canPlace(newShape, spawnX, spawnY))
            return false;

        shape = newShape;
        x = spawnX;
        y = spawnY;

        rebuildLayer();

        return true;
    }

    public boolean tryMove(int dx, int dy)
    {
        if (!hasPiece())
            return false;

        int newX = x + dx;
        int newY = y + dy;

        if (!board.canPlace(shape, newX, newY))
            return false;

        x = newX;
        y = newY;

        rebuildLayer();

        return true;
    }
    public boolean tryOneLineDown()
    {return tryMove(0, 1);}
    public boolean tryMoveLeft()
    {return tryMove(-1, 0);}
    public boolean tryMoveRight()
    {return tryMove(1, 0);}
    public int hardDrop()
    {
        int moved = 0;
        while (tryOneLineDown())
            moved++;
        return moved;
    }

    public boolean tryRotateRight()
    {
        if (!hasPiece())
            return false;

        TetrominoShape rotated = shape.rotateRight();

        if (!board.canPlace(rotated, x, y))
            return false;

        shape = rotated;
        rebuildLayer();

        return true;
    }

    public boolean tryRotateLeft()
    {
        if (!hasPiece())
            return false;

        TetrominoShape rotated = shape.rotateLeft();

        if (!board.canPlace(rotated, x, y))
            return false;

        shape = rotated;
        rebuildLayer();

        return true;
    }

    // shape 위치 변경 후 사용, activeLayer 배열에 변경 반영
    private void rebuildLayer()
    {
        clearLayer();

        if (!hasPiece())
            return;

        for (int sy = 0; sy < TetrominoShape.SIZE; sy++)
        {
            for (int sx = 0; sx < TetrominoShape.SIZE; sx++)
            {
                TetrominoEnum cell = shape.get(sx, sy);

                if (cell == TetrominoEnum.NoShape)
                    continue;

                activeLayer[y + sy][x + sx] = cell;
            }
        }
    }

    public void clear()
    {
        shape = null;
        x = 0;
        y = 0;

        clearLayer();
    }
    private void clearLayer()
    {
        for (TetrominoEnum[] row : activeLayer)
            Arrays.fill(row, TetrominoEnum.NoShape);
    }

    public boolean hasPiece()
    {
        return shape != null;
    }

    public TetrominoEnum getCell(int x, int y)
    {
        return activeLayer[y][x];
    }

    public TetrominoShape getShape() { return shape; }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getWidth() { return width; }

    public int getHeight() { return height; }
}