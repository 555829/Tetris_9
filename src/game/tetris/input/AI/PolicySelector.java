package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.tetromino.TetrominoEnum;

public class PolicySelector
{
    public class AIPolicy
    {
        public final int lineClearWeight;
        public final int holeWeight;
        public final int heightWeight;
        public final int bumpinessWeight;

        public AIPolicy(
                int lineClearWeight,
                int holeWeight,
                int heightWeight,
                int bumpinessWeight)
        {
            this.lineClearWeight = lineClearWeight;
            this.holeWeight = holeWeight;
            this.heightWeight = heightWeight;
            this.bumpinessWeight = bumpinessWeight;
        }
    }

    public AIPolicy getPolicy(Board board)
    {
        int maxHeight = getMaxHeight(board);
        int holeCount = getHoleCount(board);

        double heightRatio =
                (double) maxHeight / board.getHeight();

        if (heightRatio >= 0.7 || holeCount >= 5)
            return getDefensivePolicy();

        if (heightRatio <= 0.35 && holeCount <= 1)
            return getAggressivePolicy();

        return getNormalPolicy();
    }

    private AIPolicy getDefensivePolicy()
    {
        return new AIPolicy(
                80,     // Line Clear
                140,    // Hole
                10,     // Height
                6       // Bumpiness
        );
    }

    private AIPolicy getAggressivePolicy()
    {
        return new AIPolicy(
                160,    // Line Clear
                60,     // Hole
                2,      // Height
                2       // Bumpiness
        );
    }

    private AIPolicy getNormalPolicy()
    {
        return new AIPolicy(
                100,    // Line Clear
                90,     // Hole
                5,      // Height
                4       // Bumpiness
        );
    }

    private int getMaxHeight(Board board)
    {
        int maxHeight = 0;

        for (int x = 0; x < board.getWidth(); x++)
        {
            for (int y = 0; y < board.getHeight(); y++)
            {
                if (board.getCell(x, y) != TetrominoEnum.NoShape)
                {
                    int height = board.getHeight() - y;

                    if (height > maxHeight)
                        maxHeight = height;

                    break;
                }
            }
        }

        return maxHeight;
    }

    private int getHoleCount(Board board)
    {
        int holes = 0;

        for (int x = 0; x < board.getWidth(); x++)
        {
            boolean foundBlock = false;

            for (int y = 0; y < board.getHeight(); y++)
            {
                if (board.getCell(x, y) != TetrominoEnum.NoShape)
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
}