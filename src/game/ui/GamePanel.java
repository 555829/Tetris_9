package game.ui;

import game.tetris.Tetris;
import game.tetris.board.ActivePiece;
import game.tetris.board.Board;
import game.tetris.flow.TetrisFlow;
import game.tetris.tetromino.TetrominoEnum;
import game.tetris.tetromino.TetrominoShape;

import javax.swing.JPanel;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * Read-only view of a single Tetris game.
 * It never changes game state, so one Tetris instance per panel is enough
 * to support 1P, vs-AI and 2P layouts (just place several panels side by side).
 */
public class GamePanel extends JPanel
{
    private static final int CELL = 28;
    private static final int PREVIEW_CELL = 18;
    private static final int MARGIN = 16;
    private static final int SIDE_WIDTH = PREVIEW_CELL * TetrominoShape.SIZE + 2 * MARGIN;

    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color GRID = new Color(45, 45, 58);
    private static final Color TEXT = new Color(230, 230, 235);

    // Keyed by enum value instead of ordinal(), so adding a new shape can't silently shift colors
    // (the original Board.drawSquare indexed a color array with shape.ordinal()).
    private static final Map<TetrominoEnum, Color> COLORS = new EnumMap<>(TetrominoEnum.class);
    static
    {
        COLORS.put(TetrominoEnum.ZShape, new Color(204, 102, 102));
        COLORS.put(TetrominoEnum.SShape, new Color(102, 204, 102));
        COLORS.put(TetrominoEnum.LineShape, new Color(102, 204, 204));
        COLORS.put(TetrominoEnum.TShape, new Color(170, 102, 204));
        COLORS.put(TetrominoEnum.SquareShape, new Color(204, 204, 102));
        COLORS.put(TetrominoEnum.LShape, new Color(218, 140, 60));
        COLORS.put(TetrominoEnum.MirroredLShape, new Color(102, 102, 204));
    }

    private final Tetris game;

    public GamePanel(Tetris game)
    {
        this.game = game;

        int boardW = game.board.getWidth() * CELL;
        int boardH = game.board.getHeight() * CELL;
        setPreferredSize(new Dimension(SIDE_WIDTH + boardW + SIDE_WIDTH, boardH + 2 * MARGIN));
        setBackground(BACKGROUND);
    }

    @Override
    protected void paintComponent(Graphics g0)
    {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardX = SIDE_WIDTH;
        int boardY = MARGIN;

        drawBoard(g, boardX, boardY);
        drawHold(g, MARGIN, boardY);
        drawStats(g, MARGIN, boardY + 140);
        drawNext(g, boardX + game.board.getWidth() * CELL + MARGIN, boardY);
        drawOverlay(g, boardX, boardY);
    }

    private void drawBoard(Graphics2D g, int originX, int originY)
    {
        Board board = game.board;
        ActivePiece piece = game.activePiece;

        g.setColor(GRID);
        for (int y = 0; y < board.getHeight(); y++)
            for (int x = 0; x < board.getWidth(); x++)
                g.drawRect(originX + x * CELL, originY + y * CELL, CELL, CELL);

        for (int y = 0; y < board.getHeight(); y++)
        {
            for (int x = 0; x < board.getWidth(); x++)
            {
                // The falling piece lives on its own layer, so it must be drawn over locked cells.
                TetrominoEnum cell = piece.getCell(x, y);
                if (cell == TetrominoEnum.NoShape)
                    cell = board.getCell(x, y);

                if (cell != TetrominoEnum.NoShape)
                    drawCell(g, originX + x * CELL, originY + y * CELL, CELL, cell);
            }
        }
    }

    private void drawHold(Graphics2D g, int x, int y)
    {
        drawLabel(g, "HOLD", x, y);
        drawPreview(g, game.flow.getHoldPiece(), x, y + 10);
    }

    private void drawNext(Graphics2D g, int x, int y)
    {
        drawLabel(g, "NEXT", x, y);

        TetrominoEnum[] queue = game.queue.getQueue();
        for (int i = 0; i < queue.length; i++)
            drawPreview(g, queue[i], x, y + 10 + i * (PREVIEW_CELL * 3 + 8));
    }

    private void drawStats(Graphics2D g, int x, int y)
    {
        TetrisFlow flow = game.flow;
        g.setColor(TEXT);
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString("SCORE", x, y);
        g.drawString(String.valueOf(flow.getScore()), x, y + 18);
        g.drawString("LEVEL", x, y + 50);
        g.drawString(String.valueOf(flow.getLevel()), x, y + 68);
        g.drawString("LINES", x, y + 100);
        g.drawString(String.valueOf(flow.getLines()), x, y + 118);
    }

    private void drawPreview(Graphics2D g, TetrominoEnum type, int x, int y)
    {
        if (type == null || type == TetrominoEnum.NoShape)
            return;

        TetrominoShape shape = new TetrominoShape(type);
        for (int sy = 0; sy < TetrominoShape.SIZE; sy++)
            for (int sx = 0; sx < TetrominoShape.SIZE; sx++)
                if (shape.get(sx, sy) != TetrominoEnum.NoShape)
                    drawCell(g, x + sx * PREVIEW_CELL, y + sy * PREVIEW_CELL, PREVIEW_CELL, type);
    }

    private void drawOverlay(Graphics2D g, int boardX, int boardY)
    {
        TetrisFlow flow = game.flow;
        String message;
        if (flow.isGameOver())
            message = "GAME OVER";
        else if (flow.isMenuOpen())
            message = "MENU";
        else if (flow.isPaused())
            message = "PAUSED";
        else
            return;

        int w = game.board.getWidth() * CELL;
        int h = game.board.getHeight() * CELL;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(boardX, boardY, w, h);

        g.setColor(TEXT);
        g.setFont(getFont().deriveFont(Font.BOLD, 26f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(message, boardX + (w - fm.stringWidth(message)) / 2, boardY + h / 2);
    }

    private void drawLabel(Graphics2D g, String text, int x, int y)
    {
        g.setColor(TEXT);
        g.setFont(getFont().deriveFont(Font.BOLD, 13f));
        g.drawString(text, x, y);
    }

    private static void drawCell(Graphics2D g, int x, int y, int size, TetrominoEnum type)
    {
        Color base = COLORS.getOrDefault(type, Color.GRAY);
        g.setColor(base);
        g.fillRect(x + 1, y + 1, size - 2, size - 2);
        g.setColor(base.brighter());
        g.drawLine(x + 1, y + 1, x + size - 2, y + 1);
        g.drawLine(x + 1, y + 1, x + 1, y + size - 2);
        g.setColor(base.darker());
        g.drawLine(x + 1, y + size - 2, x + size - 2, y + size - 2);
        g.drawLine(x + size - 2, y + 1, x + size - 2, y + size - 2);
    }
}