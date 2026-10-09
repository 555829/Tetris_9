package game.tetris.input.AI;

import game.tetris.board.Board;

public class PlacementEvaluator {
    public PieceInfo getBestEvaluatedPlacement(Board board, PieceInfo[] availablePlacements, PolicySelector.AIPolicy policy)
    {
        float bestScore = Float.NEGATIVE_INFINITY;
        PieceInfo bestPieceInfo = null;
        for(PieceInfo i : availablePlacements)
        {
            i.score = getScore(board, i, policy);
            if(i.score > bestScore)
            {
                bestScore = i.score;
                bestPieceInfo = i;
            }
        }
        return bestPieceInfo;
    }

    private int getScore(Board board, PieceInfo availablePlacement, PolicySelector.AIPolicy policy)
    {
        SimulatedBoard result = new SimulatedBoard(board, availablePlacement);

        int score = 0;

        score += result.getClearedLines()
                * policy.lineClearWeight;

        score -= result.getHoleCount()
                * policy.holeWeight;

        score -= result.getAggregateHeight()
                * policy.heightWeight;

        score -= result.getBumpiness()
                * policy.bumpinessWeight;

        return score;
    }
}
