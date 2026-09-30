package game.tetris.render;

import game.tetris.board.Board;
import game.tetris.board.ActivePiece;
import game.tetris.flow.TetrisFlow;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetrominoQueue.TetrominoQueue;

public final class TetrisTextRenderer
{
    private final Board board;
    private final ActivePiece activePiece;
    private final TetrominoQueue queue;
    private final TetrisFlow gameFlow;

    public TetrisTextRenderer(
            Board board,
            ActivePiece activePiece,
            TetrominoQueue queue,
            TetrisFlow gameFlow)
    {
        this.board = board;
        this.activePiece = activePiece;
        this.queue = queue;
        this.gameFlow = gameFlow;
    }

    public String render()
    {
        StringBuilder result = new StringBuilder();

        result.append(renderStatus());
        result.append(renderBoard());
        result.append(renderQueue());

        return result.toString();
    }

    private String renderStatus()
    {
        StringBuilder result = new StringBuilder();

        result.append("=== TETRIS ===\n");

        result.append("Score : ")
                .append(gameFlow.getScore()).append('\n');

        result.append("Level : ")
                .append(gameFlow.getLevel()).append('\n');

        result.append("Lines : ")
                .append(gameFlow.getLines()).append('\n');

        result.append("Hold  : ")
                .append(toChar(gameFlow.getHoldPiece()))
                .append("\n\n");

        if (gameFlow.isGameOver())
            result.append("GAME OVER\n\n");
        else if (gameFlow.isMenuOpen())
            result.append("MENU OPEN\n\n");
        else if (gameFlow.isPaused())
            result.append("PAUSED\n\n");

        return result.toString();
    }

    private String renderBoard()
    {
        StringBuilder result = new StringBuilder();

        int width = board.getWidth();
        int height = board.getHeight();

        appendBorder(result, width);

        for (int y = 0; y < height; y++)
        {
            result.append('|');

            for (int x = 0; x < width; x++)
            {
                TetrominoEnum active =
                        activePiece.getCell(x, y);

                TetrominoEnum fixed = board.getCell(x, y);

                char cell;

                if (active != TetrominoEnum.NoShape)
                    cell = toChar(active);
                else
                    cell = Character.toLowerCase(
                            toChar(fixed)
                    );

                result.append(cell).append(' ');
            }

            result.append("|\n");
        }

        appendBorder(result, width);

        return result.toString();
    }

    private void appendBorder(StringBuilder result, int width)
    {
        result.append('+');

        for (int x = 0; x < width; x++)
            result.append("--");

        result.append("+\n");
    }

    private String renderQueue()
    {
        StringBuilder result = new StringBuilder();

        result.append("\nNext : ");

        for (TetrominoEnum type : queue.getQueue())
        {
            result.append(toChar(type)).append(' ');
        }

        result.append('\n');

        return result.toString();
    }

    private char toChar(TetrominoEnum type)
    {
        switch (type)
        {
            case ZShape:
                return 'Z';

            case SShape:
                return 'S';

            case LineShape:
                return 'I';

            case TShape:
                return 'T';

            case SquareShape:
                return 'O';

            case LShape:
                return 'L';

            case MirroredLShape:
                return 'J';

            case NoShape:
                return '.';

            default:
                throw new IllegalArgumentException();
        }
    }
}