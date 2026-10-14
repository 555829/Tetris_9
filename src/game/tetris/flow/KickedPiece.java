package game.tetris.flow;

import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

// 차여서 날아가는 중인 블록

public final class KickedPiece
{
    private final TetrominoShape shape;
    private final int totalFrames;

    private final int minCol, maxCol, minRow, maxRow;

    private double x, y;
    private double vx, vy;
    private int framesLeft;

    KickedPiece(TetrominoShape shape, int startX, int startY,
                double vx, double vy, int totalFrames)
    {
        this.shape = shape;
        this.x = startX;
        this.y = startY;
        this.vx = vx;
        this.vy = vy;
        this.totalFrames = totalFrames;
        this.framesLeft = totalFrames;

        int left = TetrominoShape.SIZE, right = -1, top = TetrominoShape.SIZE, bottom = -1;
        for (int sy = 0; sy < TetrominoShape.SIZE; sy++)
        {
            for (int sx = 0; sx < TetrominoShape.SIZE; sx++)
            {
                if (shape.get(sx, sy) == TetrominoEnum.NoShape)
                    continue;
                left = Math.min(left, sx);
                right = Math.max(right, sx);
                top = Math.min(top, sy);
                bottom = Math.max(bottom, sy);
            }
        }
        minCol = left;
        maxCol = right;
        minRow = top;
        maxRow = bottom;
    }

    // 한 프레임 이동. 벽에 닿으면 그 방향 속도만 반대로 바꿈
    boolean tick(int boardWidth, int boardHeight)
    {
        x += vx;
        y += vy;

        if (x + minCol < 0)
        {
            x = -minCol;
            vx = -vx;
        }
        else if (x + maxCol + 1 > boardWidth)
        {
            x = boardWidth - maxCol - 1;
            vx = -vx;
        }

        if (y + minRow < 0)
        {
            y = -minRow;
            vy = -vy;
        }
        else if (y + maxRow + 1 > boardHeight)
        {
            y = boardHeight - maxRow - 1;
            vy = -vy;
        }

        framesLeft--;
        return framesLeft > 0;
    }

    public TetrominoShape getShape() { return shape; }

    public double getX() { return x; }

    public double getY() { return y; }

    // 남은 시간 비율
    public double getRemainingRatio()
    {
        return (double) framesLeft / totalFrames;
    }
}