package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.tetromino.RotationState;
import game.tetris.tetromino.TetrominoShape;

import java.util.ArrayList;
import java.util.List;

public class PlacementFinder
{
    private final PlacementPathGenerator pathGenerator;

    public PlacementFinder(PlacementPathGenerator pathGenerator)
    {
        this.pathGenerator = pathGenerator;
    }

    public PieceInfo[] getAvailablePlacements(Board board, PieceInfo piece)
    {
            List<PieceInfo> placements = new ArrayList<>();

            for (RotationState rotation : RotationState.values())
            {
                TetrominoShape shape = new TetrominoShape(piece.tetromino, rotation);

                int minX = -(TetrominoShape.SIZE - 1);
            int maxX = board.getWidth() - 1;

            int minY = -(TetrominoShape.SIZE - 1);
            int maxY = board.getHeight() - 1;

            for (int y = minY; y <= maxY; y++)
            {
                for (int x = minX; x <= maxX; x++)
                {
                    if (!board.canPlace(shape, x, y))
                        continue;
                    if (board.canPlace(shape, x, y + 1))
                        continue;

                    PieceInfo placement = new PieceInfo();
                    placement.tetromino = piece.tetromino;
                    placement.rotation = rotation;
                    placement.x = x;
                    placement.y = y;

                    if (pathGenerator.getPath(board, piece, placement) == null)
                        continue;

                    placements.add(placement);
                }
            }
        }

        return placements.toArray(new PieceInfo[0]);
    }
}