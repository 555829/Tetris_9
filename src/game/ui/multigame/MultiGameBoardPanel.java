package game.ui.multigame;

import game.ui.common.UIUtils;
import game.ui.multigame.engine.Tetris;
import game.ui.multigame.engine.tetromino.TetrominoEnum;
import game.ui.multigame.engine.tetromino.TetrominoShape;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.EnumMap;
import java.util.Map;

/** A multiplayer-owned board view with the same gameplay HUD as the single-player screen. */
final class MultiGameBoardPanel extends JPanel {
    private static final int CELL = 18;
    private static final int PREVIEW_CELL = 9;
    private static final int MARGIN = 10;
    private static final int SIDE_WIDTH = PREVIEW_CELL * TetrominoShape.SIZE + 2 * MARGIN;
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;

    private static final Color GRID = new Color(45, 45, 58);
    private static final Color TEXT = new Color(230, 230, 235);
    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Map<TetrominoEnum, Color> COLORS = new EnumMap<>(TetrominoEnum.class);

    static {
        COLORS.put(TetrominoEnum.ZShape, new Color(204, 102, 102));
        COLORS.put(TetrominoEnum.SShape, new Color(102, 204, 102));
        COLORS.put(TetrominoEnum.LineShape, new Color(102, 204, 204));
        COLORS.put(TetrominoEnum.TShape, new Color(170, 102, 204));
        COLORS.put(TetrominoEnum.SquareShape, new Color(204, 204, 102));
        COLORS.put(TetrominoEnum.LShape, new Color(218, 140, 60));
        COLORS.put(TetrominoEnum.MirroredLShape, new Color(102, 102, 204));
    }

    private final Tetris game;

    MultiGameBoardPanel(Tetris game) {
        this.game = game;
        setOpaque(true);
        setBackground(BACKGROUND);
        setPreferredSize(sizeFor(BOARD_WIDTH, BOARD_HEIGHT));
        setMinimumSize(new Dimension(100, 100));
    }

    private static Dimension sizeFor(int boardWidth, int boardHeight) {
        return new Dimension(SIDE_WIDTH * 2 + boardWidth * CELL,
                boardHeight * CELL + 2 * MARGIN);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (game == null || getWidth() <= 0 || getHeight() <= 0) return;

        Graphics2D g = (Graphics2D) graphics.create();
        double scale = Math.min(getWidth() / (double) sizeFor(BOARD_WIDTH, BOARD_HEIGHT).width,
                getHeight() / (double) sizeFor(BOARD_WIDTH, BOARD_HEIGHT).height);
        if (!Double.isFinite(scale) || scale <= 0) {
            g.dispose();
            return;
        }

        int baseWidth = sizeFor(BOARD_WIDTH, BOARD_HEIGHT).width;
        int baseHeight = sizeFor(BOARD_WIDTH, BOARD_HEIGHT).height;
        g.translate((getWidth() - baseWidth * scale) / 2.0,
                (getHeight() - baseHeight * scale) / 2.0);
        g.scale(scale, scale);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardX = SIDE_WIDTH;
        int boardY = MARGIN;
        drawBoard(g, boardX, boardY);
        drawHold(g, MARGIN, boardY);
        drawStats(g, MARGIN, boardY + 130);
        drawNext(g, boardX + BOARD_WIDTH * CELL + MARGIN, boardY);
        drawGameOver(g, boardX, boardY);
        g.dispose();
    }

    private void drawBoard(Graphics2D g, int originX, int originY) {
        g.setColor(GRID);
        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                g.drawRect(originX + x * CELL, originY + y * CELL, CELL, CELL);
            }
        }

        for (int y = 0; y < BOARD_HEIGHT; y++) {
            for (int x = 0; x < BOARD_WIDTH; x++) {
                TetrominoEnum type = game.activePiece.getCell(x, y);
                if (type == TetrominoEnum.NoShape) type = game.board.getCell(x, y);
                if (type != TetrominoEnum.NoShape) {
                    drawCell(g, originX + x * CELL, originY + y * CELL, CELL, type);
                }
            }
        }
    }

    private void drawHold(Graphics2D g, int x, int y) {
        drawLabel(g, "홀드", x, y);
        drawPreview(g, game.flow.getHoldPiece(), x, y + 10);
    }

    private void drawNext(Graphics2D g, int x, int y) {
        drawLabel(g, "다음", x, y);
        TetrominoEnum[] queue = game.queue.getQueue();
        for (int i = 0; i < queue.length; i++) {
            drawPreview(g, queue[i], x, y + 10 + i * (PREVIEW_CELL * 3 + 8));
        }
    }

    private void drawStats(Graphics2D g, int x, int y) {
        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 13f));
        g.drawString("점수", x, y);
        g.drawString(String.valueOf(game.flow.getScore()), x, y + 18);
        g.drawString("레벨", x, y + 50);
        g.drawString(String.valueOf(game.flow.getLevel()), x, y + 68);
        g.drawString("줄", x, y + 100);
        g.drawString(String.valueOf(game.flow.getLines()), x, y + 118);
    }

    private void drawPreview(Graphics2D g, TetrominoEnum type, int x, int y) {
        if (type == null || type == TetrominoEnum.NoShape) return;
        TetrominoShape shape = new TetrominoShape(type);
        for (int sy = 0; sy < TetrominoShape.SIZE; sy++) {
            for (int sx = 0; sx < TetrominoShape.SIZE; sx++) {
                if (shape.get(sx, sy) != TetrominoEnum.NoShape) {
                    drawCell(g, x + sx * PREVIEW_CELL, y + sy * PREVIEW_CELL,
                            PREVIEW_CELL, type);
                }
            }
        }
    }

    private void drawGameOver(Graphics2D g, int boardX, int boardY) {
        if (!game.flow.isGameOver()) return;
        int width = BOARD_WIDTH * CELL;
        int height = BOARD_HEIGHT * CELL;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(boardX, boardY, width, height);
        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 26f));
        FontMetrics metrics = g.getFontMetrics();
        String message = "탈락";
        g.drawString(message, boardX + (width - metrics.stringWidth(message)) / 2,
                boardY + height / 2);
    }

    private void drawLabel(Graphics2D g, String label, int x, int y) {
        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 13f));
        g.drawString(label, x, y);
    }

    private static void drawCell(Graphics2D g, int x, int y, int size, TetrominoEnum type) {
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
