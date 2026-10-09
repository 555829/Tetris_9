package game.tetris.input.AI;

import game.tetris.board.Board;
import game.tetris.input.InputEnum;
import game.tetris.tetromino.RotationState;
import game.tetris.tetromino.TetrominoShape;

import java.util.*;


public class PlacementPathGenerator
{
    public InputEnum[] addHold(InputEnum[] path)
    {
        InputEnum[] result = new InputEnum[path.length + 1];

        result[0] = InputEnum.Hold;
        System.arraycopy(path, 0, result, 1, path.length);

        return result;
    }
    public InputEnum[] getPath(Board board, PieceInfo curPiece, PieceInfo target)
    {
        Queue<PathNode> queue = new ArrayDeque<>();
        Set<StateKey> visited = new HashSet<>();

        PathNode start = new PathNode(copyPiece(curPiece), new ArrayList<>());

        queue.add(start);
        visited.add(new StateKey(curPiece));

        while (!queue.isEmpty())
        {
            PathNode current = queue.poll();

            if (isSamePlacement(current.piece, target))
            {
                return current.path.toArray(new InputEnum[0]);
            }

            tryAddMove(
                    board,
                    current,
                    InputEnum.Left,
                    queue,
                    visited
            );

            tryAddMove(
                    board,
                    current,
                    InputEnum.Right,
                    queue,
                    visited
            );

            tryAddMove(
                    board,
                    current,
                    InputEnum.Rotate,
                    queue,
                    visited
            );

            tryAddMove(
                    board,
                    current,
                    InputEnum.OneLineDown,
                    queue,
                    visited
            );
        }

        return null;
    }

    private void tryAddMove(
            Board board,
            PathNode current,
            InputEnum input,
            Queue<PathNode> queue,
            Set<StateKey> visited)
    {
        PieceInfo nextPiece = copyPiece(current.piece);

        if (!applyInput(board, nextPiece, input))
            return;

        StateKey key = new StateKey(nextPiece);

        if (visited.contains(key))
            return;

        visited.add(key);
        List<InputEnum> nextPath = new ArrayList<>(current.path);
        nextPath.add(input);
        queue.add(new PathNode(nextPiece, nextPath));
    }

    private boolean applyInput(Board board, PieceInfo piece, InputEnum input)
    {
        switch (input)
        {
            case Left:
                return tryMove(board, piece, -1, 0);
            case Right:
                return tryMove(board, piece, 1, 0);
            case OneLineDown:
                return tryMove(board, piece, 0, 1);
            case Rotate:
                return tryRotateRight(board, piece);
            default:
                return false;
        }
    }

    private boolean tryMove(Board board, PieceInfo piece, int dx, int dy)
    {
        int newX = piece.x + dx;
        int newY = piece.y + dy;

        TetrominoShape shape = getShape(piece);

        if (!board.canPlace(shape, newX, newY))
        {
            return false;
        }

        piece.x = newX;
        piece.y = newY;

        return true;
    }

    private boolean tryRotateRight(Board board, PieceInfo piece)
    {
        TetrominoShape shape = getShape(piece);
        TetrominoShape rotated = shape.rotateRight();

        if (!board.canPlace(rotated, piece.x, piece.y))
        {
            return false;
        }

        piece.rotation = piece.rotation.rotateRight();

        return true;
    }

    private TetrominoShape getShape(PieceInfo piece)
    {
        TetrominoShape shape = new TetrominoShape(piece.tetromino);
        int rotationCount = getRotationCount(piece.rotation);

        for (int i = 0; i < rotationCount; i++)
            shape = shape.rotateRight();

        return shape;
    }

    private int getRotationCount(RotationState rotation)
    {
        return switch (rotation)
        {
            case SPAWN -> 0;
            case RIGHT -> 1;
            case REVERSE -> 2;
            case LEFT -> 3;
        };
    }
    private boolean isSamePlacement(PieceInfo a, PieceInfo b)
    {
        return a.x == b.x &&
                a.y == b.y &&
                a.rotation == b.rotation &&
                a.tetromino == b.tetromino;
    }

    private PieceInfo copyPiece(PieceInfo source)
    {
        PieceInfo copy = new PieceInfo();

        copy.tetromino = source.tetromino;
        copy.rotation = source.rotation;
        copy.x = source.x;
        copy.y = source.y;
        copy.score = source.score;

        return copy;
    }

    private static class PathNode
    {
        PieceInfo piece;
        List<InputEnum> path;

        PathNode(PieceInfo piece, List<InputEnum> path)
        {
            this.piece = piece;
            this.path = path;
        }
    }

    private static class StateKey
    {
        int x;
        int y;
        RotationState rotation;

        StateKey(PieceInfo piece)
        {
            x = piece.x;
            y = piece.y;
            rotation = piece.rotation;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;

            if (!(obj instanceof StateKey))
                return false;

            StateKey other = (StateKey) obj;

            return x == other.x &&
                    y == other.y &&
                    rotation == other.rotation;
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(x, y, rotation);
        }
    }
}