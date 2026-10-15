package game.ui.multigame;

import game.ui.multigame.engine.Tetris;
import game.ui.multigame.engine.board.ActivePiece;
import game.ui.multigame.engine.board.Board;
import game.ui.multigame.engine.flow.TetrisFlow;
import game.ui.multigame.engine.tetromino.TetrominoEnum;
import game.ui.multigame.engine.tetromino.TetrominoShape;
import game.ui.common.UIUtils;

import javax.swing.JPanel;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

public class MultiGameResult extends JPanel {
    private static final int CELL = 28;
    private static final int PREVIEW_CELL = 18;
    private static final int MARGIN = 16;
    private static final int SIDE_WIDTH = PREVIEW_CELL * TetrominoShape.SIZE + 2 * MARGIN;

    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color GRID = new Color(45, 45, 58);
    private static final Color TEXT = new Color(230, 230, 235);

    // 블록 종류별 색상 매핑
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

    // 로컬 게임 객체 (로컬 유저/봇일 때 사용)
    private final Tetris game;

    // 원격 네트워크 플레이어용 동기화 데이터 (game이 null일 때 사용)
    private final int[][] remoteBoard = new int[20][10];
    private int remoteScore = 0;
    private int remoteLevel = 1;
    private int remoteLines = 0;
    private boolean isRemoteGameOver = false;

    // 1. 로컬 플레이어 / 봇 전용 생성자
    public MultiGameResult(Tetris game) {
        this.game = game;
        int width = (game != null) ? game.board.getWidth() : 10;
        int height = (game != null) ? game.board.getHeight() : 20;

        setPreferredSize(sizeFor(width, height));
        setBackground(BACKGROUND);
    }

    // 2. 원격 네트워크 플레이어 전용 생성자 (기본 10x20 규격)
    public MultiGameResult() {
        this(null);
    }

    // 패널 권장 크기 계산
    public static Dimension sizeFor(int boardWidth, int boardHeight) {
        return new Dimension(2 * SIDE_WIDTH + boardWidth * CELL, boardHeight * CELL + 2 * MARGIN);
    }

    // ==============================================================================================================
    // 원격 패킷 데이터 수신 및 동기화 메서드
    // ==============================================================================================================
    public void updateRemoteBoard(String boardDataStr) {
        if (boardDataStr == null || boardDataStr.length() < 200) return;

        int idx = 0;
        for (int r = 0; r < 20; r++) {
            for (int c = 0; c < 10; c++) {
                remoteBoard[r][c] = boardDataStr.charAt(idx++) - '0';
            }
        }
        repaint();
    }

    public void updateRemoteStats(int score, int level, int lines) {
        this.remoteScore = score;
        this.remoteLevel = level;
        this.remoteLines = lines;
        repaint();
    }

    public void setRemoteGameOver(boolean isGameOver) {
        this.isRemoteGameOver = isGameOver;
        repaint();
    }

    // ==============================================================================================================
    // 렌더링 (paintComponent)
    // ==============================================================================================================

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int boardX = SIDE_WIDTH;
        int boardY = MARGIN;

        int boardW = (game != null) ? game.board.getWidth() : 10;
        int boardH = (game != null) ? game.board.getHeight() : 20;

        drawBoard(g, boardX, boardY, boardW, boardH);
        drawHold(g, MARGIN, boardY);
        drawStats(g, MARGIN, boardY + 140);
        drawNext(g, boardX + boardW * CELL + MARGIN, boardY);
        drawOverlay(g, boardX, boardY, boardW, boardH);
    }

    private void drawBoard(Graphics2D g, int originX, int originY, int width, int height) {
        // 1. 격자선 그리기
        g.setColor(GRID);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                g.drawRect(originX + x * CELL, originY + y * CELL, CELL, CELL);
            }
        }

        // 2. 블록 그리기
        if (game != null) {
            // [로컬 모드] 실시간 Tetris 객체 참조
            Board board = game.board;
            ActivePiece piece = game.activePiece;

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    TetrominoEnum cell = piece.getCell(x, y);
                    if (cell == TetrominoEnum.NoShape) {
                        cell = board.getCell(x, y);
                    }
                    if (cell != TetrominoEnum.NoShape) {
                        drawCell(g, originX + x * CELL, originY + y * CELL, CELL, cell);
                    }
                }
            }
        } else {
            // [원격 모드] 수신받은 remoteBoard 2D 배열 참조
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int val = remoteBoard[y][x];
                    if (val > 0) {
                        TetrominoEnum type = getEnumByOrdinal(val);
                        drawCell(g, originX + x * CELL, originY + y * CELL, CELL, type);
                    }
                }
            }
        }
    }

    private void drawHold(Graphics2D g, int x, int y) {
        drawLabel(g, "홀드", x, y);
        if (game != null && game.flow != null) {
            drawPreview(g, game.flow.getHoldPiece(), x, y + 10);
        }
    }

    private void drawNext(Graphics2D g, int x, int y) {
        drawLabel(g, "다음", x, y);

        if (game != null && game.queue != null) {
            TetrominoEnum[] queue = game.queue.getQueue();
            for (int i = 0; i < queue.length; i++) {
                drawPreview(g, queue[i], x, y + 10 + i * (PREVIEW_CELL * 3 + 8));
            }
        }
    }

    private void drawStats(Graphics2D g, int x, int y) {
        int score = (game != null) ? game.flow.getScore() : remoteScore;
        int level = (game != null) ? game.flow.getLevel() : remoteLevel;
        int lines = (game != null) ? game.flow.getLines() : remoteLines;

        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 13f));
        g.drawString("점수", x, y);
        g.drawString(String.valueOf(score), x, y + 18);
        g.drawString("레벨", x, y + 50);
        g.drawString(String.valueOf(level), x, y + 68);
        g.drawString("줄", x, y + 100);
        g.drawString(String.valueOf(lines), x, y + 118);
    }

    private void drawPreview(Graphics2D g, TetrominoEnum type, int x, int y) {
        if (type == null || type == TetrominoEnum.NoShape) return;

        TetrominoShape shape = new TetrominoShape(type);
        for (int sy = 0; sy < TetrominoShape.SIZE; sy++) {
            for (int sx = 0; sx < TetrominoShape.SIZE; sx++) {
                if (shape.get(sx, sy) != TetrominoEnum.NoShape) {
                    drawCell(g, x + sx * PREVIEW_CELL, y + sy * PREVIEW_CELL, PREVIEW_CELL, type);
                }
            }
        }
    }

    private void drawOverlay(Graphics2D g, int boardX, int boardY, int width, int height) {
        String message = null;

        if (game != null) {
            TetrisFlow flow = game.flow;
            if (flow.isGameOver()) message = "게임 오버";
            else if (flow.isMenuOpen()) message = "메뉴";
            else if (flow.isPaused()) message = "일시정지";
        } else {
            if (isRemoteGameOver) message = "게임 오버";
        }

        if (message == null) return;

        int w = width * CELL;
        int h = height * CELL;
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(boardX, boardY, w, h);

        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 26f));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(message, boardX + (w - fm.stringWidth(message)) / 2, boardY + h / 2);
    }

    private void drawLabel(Graphics2D g, String text, int x, int y) {
        g.setColor(TEXT);
        g.setFont(UIUtils.getFont(Font.BOLD, 13f));
        g.drawString(text, x, y);
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

    // 정수 번호(ordinal)를 TetrominoEnum으로 변환해주는 도우미 메서드
    private TetrominoEnum getEnumByOrdinal(int ordinal) {
        TetrominoEnum[] values = TetrominoEnum.values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return TetrominoEnum.NoShape;
    }
}
