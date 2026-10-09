package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

import java.util.Arrays;

public class SimulatedBoard
{
    private final int width;
    private final int height;
    private final TetrominoEnum[][] board;

    private int clearedLines;

    public SimulatedBoard(Board originalBoard, PieceInfo placement)
    {
        width = originalBoard.getWidth();
        height = originalBoard.getHeight();

        board = new TetrominoEnum[height][width];

        copyBoard(originalBoard);

        TetrominoShape shape = new TetrominoShape(placement.tetromino, placement.rotation);

        placePiece(shape, placement.x, placement.y);
        clearedLines = clearAllLines();
    }

    private void copyBoard(Board originalBoard)
    {
        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                board[y][x] = originalBoard.getCell(x, y);
            }
        }
    }

    private void placePiece(
            TetrominoShape shape,
            int posX,
            int posY)
    {
        for (int y = 0; y < TetrominoShape.SIZE; y++)
        {
            for (int x = 0; x < TetrominoShape.SIZE; x++)
            {
                TetrominoEnum cell = shape.get(x, y);

                if (cell == TetrominoEnum.NoShape)
                    continue;

                int boardX = posX + x;
                int boardY = posY + y;

                board[boardY][boardX] = cell;
            }
        }
    }

    private int clearAllLines()
    {
        int cleared = 0;
        int writeY = height - 1;

        for (int readY = height - 1; readY >= 0; readY--)
        {
            if (isLineFull(readY))
            {
                cleared++;
                continue;
            }

            if (writeY != readY)
            {
                System.arraycopy(
                        board[readY], 0,
                        board[writeY], 0,
                        width
                );
            }

            writeY--;
        }

        for (int y = writeY; y >= 0; y--)
        {
            Arrays.fill(
                    board[y],
                    TetrominoEnum.NoShape
            );
        }

        return cleared;
    }

    private boolean isLineFull(int y)
    {
        for (int x = 0; x < width; x++)
        {
            if (board[y][x] == TetrominoEnum.NoShape)
                return false;
        }

        return true;
    }




    public int getHoleCount()
    {
        int holes = 0;

        for (int x = 0; x < width; x++)
        {
            boolean foundBlock = false;

            for (int y = 0; y < height; y++)
            {
                if (board[y][x] != TetrominoEnum.NoShape)
                {
                    foundBlock = true;
                }
                else if (foundBlock)
                {
                    holes++;
                }
            }
        }

        return holes;
    }




    public int getAggregateHeight()
    {
        int result = 0;

        for (int x = 0; x < width; x++)
        {
            result += getColumnHeight(x);
        }

        return result;
    }




    public int getBumpiness()
    {
        int result = 0;

        for (int x = 0; x < width - 1; x++)
        {
            int currentHeight = getColumnHeight(x);
            int nextHeight = getColumnHeight(x + 1);

            result += Math.abs(
                    currentHeight - nextHeight
            );
        }

        return result;
    }

    private int getColumnHeight(int x)
    {
        for (int y = 0; y < height; y++)
        {
            if (board[y][x] != TetrominoEnum.NoShape)
            {
                return height - y;
            }
        }

        return 0;
    }

    public int getClearedLines()
    {
        return clearedLines;
    }

    public TetrominoEnum getCell(int x, int y)
    {
        return board[y][x];
    }
}