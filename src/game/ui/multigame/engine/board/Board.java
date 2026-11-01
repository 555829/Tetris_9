package game.ui.multigame.engine.board;

import game.ui.multigame.engine.tetromino.TetrominoEnum;
import game.ui.multigame.engine.tetromino.TetrominoShape;

import java.util.Arrays;

public final class Board
{
    private final int width;
    private final int height;
    private final TetrominoEnum[][] board;
    /*
         0 1 2 3 ...
         1
         2
         3
         ...
     */

    public Board(int width, int height)
    {
        game.rules.BoardDimensions.validate(width, height);
        this.width = width;
        this.height = height;

        board = new TetrominoEnum[height][width];

        clear();
    }

    // 매개 위치에 매개 shape를 놓을 수 있는지 검사
    public boolean canPlace(TetrominoShape shape, int posX, int posY)
    {
        for (int y = 0; y < TetrominoShape.SIZE; y++)
        {
            for (int x = 0; x < TetrominoShape.SIZE; x++)
            {
                //NoShape면 검사하지 않음
                if (shape.get(x, y) == TetrominoEnum.NoShape)
                    continue;

                int boardX = posX + x;
                int boardY = posY + y;

                if (boardX < 0 || boardX >= width ||
                        boardY < 0 || boardY >= height)
                    return false;

                if (board[boardY][boardX] != TetrominoEnum.NoShape)
                    return false;
            }
        }

        return true;
    }

    // activePiece를 board에 결합 후 라인 클리어
    public int lockPieceAndClearAllLines(ActivePiece activePiece)
    {
        if (!activePiece.hasPiece())
            throw new IllegalStateException("No active piece");

        if (activePiece.getWidth() != width ||
                activePiece.getHeight() != height)
            throw new IllegalArgumentException("Board size mismatch");

        if (!canPlace(
                activePiece.getShape(),
                activePiece.getX(),
                activePiece.getY()))
            throw new IllegalStateException("Invalid piece position");

        for (int y = 0; y < height; y++)
        {
            for (int x = 0; x < width; x++)
            {
                TetrominoEnum cell = activePiece.getCell(x, y);

                if (cell != TetrominoEnum.NoShape)
                    board[y][x] = cell;
            }
        }

        // 이 밑으로 완성된 줄 제거 및 남은 줄 아래로 이동
        int clearedLines = 0;
        int writeY = height - 1;

        for (int readY = height - 1; readY >= 0; readY--)
        {
            boolean isFull = true;

            for (int x = 0; x < width; x++)
            {
                if (board[readY][x] == TetrominoEnum.NoShape)
                {
                    isFull = false;
                    break;
                }
            }

            if (isFull)
            {
                clearedLines++;
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

        // 줄 제거 후 상단에 생긴 빈 공간 초기화
        for (int y = writeY; y >= 0; y--)
        {
            for (int x = 0; x < width; x++)
                board[y][x] = TetrominoEnum.NoShape;
        }

        return clearedLines;
    }


    public TetrominoEnum getCell(int x, int y)
    {
        return board[y][x];
    }

    public int getWidth() { return width; }

    public int getHeight() { return height; }

    //멀티플레이 아이템전 쪽 코드
    public boolean addGarbageLines(int lineCount, int holeColumn)
    {
        if (lineCount < 0 || lineCount >= height || holeColumn < 0 || holeColumn >= width)
            return false;
        if (lineCount == 0) return true;

        for (int y = 0; y < lineCount; y++)
        {
            for (TetrominoEnum cell : board[y])
                if (cell != TetrominoEnum.NoShape) return false;
        }

        for (int y = 0; y < height - lineCount; y++)
            System.arraycopy(board[y + lineCount], 0, board[y], 0, width);

        for (int y = height - lineCount; y < height; y++)
        {
            Arrays.fill(board[y], TetrominoEnum.ZShape);
            board[y][holeColumn] = TetrominoEnum.NoShape;
        }
        return true;
    }

    /** 검증한 행 우선 스냅샷으로 보드 상태를 교체합니다. */
    public boolean replaceFromSnapshot(String payload)
    {
        if (payload == null || payload.length() != width * height)
            return false;

        TetrominoEnum[] values = TetrominoEnum.values();
        TetrominoEnum[] decoded = new TetrominoEnum[payload.length()];
        for (int i = 0; i < payload.length(); i++)
        {
            int ordinal = payload.charAt(i) - 'A';
            if (ordinal < 0 || ordinal >= values.length)
                return false;
            decoded[i] = values[ordinal];
        }

        for (int i = 0; i < decoded.length; i++)
            board[i / width][i % width] = decoded[i];
        return true;
    }

    public void clear()
    {
        for (TetrominoEnum[] row : board)
            Arrays.fill(row, TetrominoEnum.NoShape);
    }
}
