package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.input.InputEnum;
import game.tetris.tetromino.RotationState;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

public class TetrisAI {
    private PolicySelector policySelector = new PolicySelector();
    private PlacementEvaluator placementEvaluator = new PlacementEvaluator();
    private PlacementPathGenerator placementPathGenerator = new PlacementPathGenerator();
    private PlacementFinder placementFinder = new PlacementFinder(placementPathGenerator);

    public InputEnum[] getMoveList(Board board, PieceInfo curPiece, TetrominoEnum holdPiece)
    {
        PolicySelector.AIPolicy policy = policySelector.getPolicy(board);

        PieceInfo holdStart = new PieceInfo();
        holdStart.tetromino = holdPiece;
        holdStart.rotation = RotationState.SPAWN;
        holdStart.x = (board.getWidth() - TetrominoShape.SIZE) / 2;
        holdStart.y = 0;

        PieceInfo[] curPiecePlacements = getPlacements(board, curPiece, false);
        PieceInfo[] holdPiecePlacements = getPlacements(board, holdStart, true);
        PieceInfo[] availablePlacements = new PieceInfo[curPiecePlacements.length + holdPiecePlacements.length];
        System.arraycopy(curPiecePlacements, 0, availablePlacements, 0, curPiecePlacements.length);
        System.arraycopy(holdPiecePlacements, 0, availablePlacements, curPiecePlacements.length, holdPiecePlacements.length);

        PieceInfo target = placementEvaluator.getBestEvaluatedPlacement(board, availablePlacements, policy);

        if (target == null)
            return new InputEnum[0];

        PieceInfo startPiece = target.useHold ? holdStart : curPiece;
        InputEnum[] path = placementPathGenerator.getPath(board, startPiece, target);
        if (target.useHold)
            return placementPathGenerator.addHold(path);
        return path;
    }

    private PieceInfo[] getPlacements(Board board, PieceInfo piece, boolean useHold)
    {
        PieceInfo[] placements = placementFinder.getAvailablePlacements(board, piece);

        for (PieceInfo placement : placements)
            placement.useHold = useHold;

        return placements;
    }

}
