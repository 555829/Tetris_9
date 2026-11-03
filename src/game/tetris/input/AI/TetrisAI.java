package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.board.SpawnPosition;
import game.tetris.input.InputEnum;
import game.tetris.tetromino.TetrominoEnum;
import java.util.logging.Logger;


public class TetrisAI {
    private PolicySelector policySelector = new PolicySelector();
    private PlacementEvaluator placementEvaluator = new PlacementEvaluator();
    private PlacementPathGenerator placementPathGenerator = new PlacementPathGenerator();
    private PlacementFinder placementFinder = new PlacementFinder(placementPathGenerator);
    private static final Logger logger =
            Logger.getLogger(TetrisAI.class.getName());

    public InputEnum[] getMoveList(Board board, PieceInfo curPiece, PieceInfo holdPiece)
    {
        // 정책 설정
        PolicySelector.AIPolicy policy = policySelector.getPolicy(board);

        // availablePlacements : 현재 piece와 hold piece 둘 모두의 후보
        PieceInfo[] curPiecePlacements = getPlacements(board, curPiece, false);
        PieceInfo[] holdPiecePlacements = getPlacements(board, holdPiece, true);

        PieceInfo[] availablePlacements = new PieceInfo[
                curPiecePlacements.length + holdPiecePlacements.length];

        System.arraycopy(curPiecePlacements, 0, availablePlacements,
                0, curPiecePlacements.length);
        System.arraycopy(holdPiecePlacements, 0, availablePlacements,
                          curPiecePlacements.length, holdPiecePlacements.length);

        // 목표 설정
        PieceInfo target = placementEvaluator.getBestEvaluatedPlacement(board, availablePlacements, policy);

        if (target == null)
        {
            logger.severe("TetrisAI : 경로를 찾지 못했습니다. AI failed to find a path.");
            return new InputEnum[0];
        }

        // 경로 설정
        PieceInfo startPiece = target.useHold ? holdPiece : curPiece;
        InputEnum[] path = placementPathGenerator.getPath(
                board, startPiece, target);

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
