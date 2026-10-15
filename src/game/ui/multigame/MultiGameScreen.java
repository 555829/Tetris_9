package game.ui.multigame;

import game.NetworkManager;
import game.SettingsManager;
import game.ui.multigame.engine.Tetris;
import game.ui.multigame.engine.board.BoardSnapshotCodec;
import game.ui.multigame.engine.input.InputSourceType;
import game.ui.multigame.engine.tetrominoQueue.PieceGenerator_7Bag;
import game.ui.multigame.engine.tetrominoQueue.TetrominoQueue;
import game.ui.common.UIUtils;
import game.ui.common.ResponsiveGridLayout;
import game.ui.outgame.MultiRoomPanel.SlotType;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

//======================================================================================================================
// 멀티플레이 화면 구현
//======================================================================================================================
public class MultiGameScreen extends JPanel {
    private static final int FRAME_MS = 1000 / 60;
    private static final int GAME_OVER_HOLD_FRAMES = 120;
    private static final int PANEL_GAP = 4;
    private static final long TIME_RESULT_GRACE_NANOS = 1_000_000_000L;

    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);

    private final Consumer<String> onFinished;
    private final Runnable onLeaveRequested;
    private final Runnable onSettingsRequested;
    private final SettingsManager settings;
    private final Timer timer = new Timer(FRAME_MS, e -> tick());

    private final Map<Integer, Tetris> allGames = new HashMap<>();
    private final Map<Integer, Tetris> localGames = new HashMap<>();
    private final Map<Integer, MultiGameBoardPanel> gamePanels = new HashMap<>();

    private final SlotType[] slotTypes = new SlotType[4];
    private final boolean[] isGameOver = new boolean[4];
    private final int[] lastReportedLines = new int[4];
    private final int[] reportedScores = new int[4];
    private final int[] reportedLines = new int[4];
    private final int[] eliminationOrder = new int[4];
    private final String[] playerNames = new String[4];
    private Consumer<String> networkListener;

    private final JPanel gridPanel;
    private final JPanel standingsRows = new JPanel();
    private final JLabel modeStatus = new JLabel("", SwingConstants.CENTER);
    private final JTextArea chatArea = new JTextArea();
    private final JTextField chatField = new JTextField();
    private final JPanel playArea = new JPanel(new BorderLayout(4, 0));
    private final JPanel chatPanel;
    private final JButton chatToggleButton;

    private int mySlotIndex = 0;
    private boolean isHost = false;
    private boolean finished = false;
    private int finishedFrames = 0;
    private int syncTick = 0;
    private int statsSyncTick = 0;
    private MultiGameMode mode = MultiGameMode.TIME_ATTACK;
    private long roundStartedAtNanos;
    private long countdownEndsAtNanos;
    private boolean waitingForServerResult;
    private boolean winnerRequestSent;
    private boolean timeResultsPending;
    private boolean timeResultsSent;
    private long timeResultsSendAtNanos;
    private String matchResultPacket;
    private int eliminationCounter;

    public MultiGameScreen(Consumer<String> onFinished) {
        this(onFinished, null);
    }

    public MultiGameScreen(Consumer<String> onFinished, SettingsManager settings) {
        this(onFinished, settings, null, null);
    }

    public MultiGameScreen(Consumer<String> onFinished, SettingsManager settings, Runnable onLeaveRequested) {
        this(onFinished, settings, onLeaveRequested, null);
    }

    public MultiGameScreen(Consumer<String> onFinished, SettingsManager settings,
                           Runnable onLeaveRequested, Runnable onSettingsRequested) {
        this.onFinished = onFinished;
        this.settings = settings;
        this.onLeaveRequested = onLeaveRequested;
        this.onSettingsRequested = onSettingsRequested;
        setBackground(BACKGROUND);
        setLayout(new BorderLayout());
        setFocusable(true);

        gridPanel = new JPanel(new ResponsiveGridLayout(4, 1, 220, PANEL_GAP, 0));
        gridPanel.setOpaque(false);
        modeStatus.setForeground(TEXT);
        modeStatus.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));

        JButton leaveButton = UIUtils.createDangerButton("방 나가기");
        leaveButton.setPreferredSize(new Dimension(142, 38));
        leaveButton.setMaximumSize(new Dimension(142, 38));
        leaveButton.setFocusable(false);
        leaveButton.addActionListener(e -> leaveGame());
        chatToggleButton = UIUtils.createSecondaryButton("채팅 숨기기");
        chatToggleButton.setPreferredSize(new Dimension(120, 38));
        chatToggleButton.setFocusable(false);
        chatToggleButton.addActionListener(e -> toggleChatPanel());
        JButton settingsButton = UIUtils.createSecondaryButton("설정");
        settingsButton.setPreferredSize(new Dimension(92, 38));
        settingsButton.setFocusable(false);
        settingsButton.addActionListener(e -> {
            if (this.onSettingsRequested != null) this.onSettingsRequested.run();
        });
        JPanel header = new JPanel(new BorderLayout(10, 0));
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(8, 10, 10, 10));
        header.add(leaveButton, BorderLayout.WEST);
        header.add(modeStatus, BorderLayout.CENTER);
        JPanel headerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        headerActions.setOpaque(false);
        headerActions.add(chatToggleButton);
        headerActions.add(settingsButton);
        header.add(headerActions, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        playArea.setOpaque(false);
        playArea.add(gridPanel, BorderLayout.CENTER);
        chatPanel = createChatPanel();
        playArea.add(chatPanel, BorderLayout.EAST);
        add(playArea, BorderLayout.CENTER);
    }

    public void startMultiGame(int mySlotIndex, boolean isHost, SlotType[] slots, int level, String modeLabel) {
        startMultiGame(mySlotIndex, isHost, slots, null, level, modeLabel);
    }

    public void startMultiGame(int mySlotIndex, boolean isHost, SlotType[] slots, String[] nicknames,
                               int level, String modeLabel) {
        stop();
        chatPanel.setVisible(true);
        chatToggleButton.setText("채팅 숨기기");

        this.mySlotIndex = mySlotIndex;
        this.isHost = isHost;
        this.finished = false;
        this.finishedFrames = 0;
        this.syncTick = 0;
        this.statsSyncTick = 0;
        this.mode = MultiGameMode.fromLabel(modeLabel);
        this.countdownEndsAtNanos = System.nanoTime() + 3_000_000_000L;
        this.roundStartedAtNanos = countdownEndsAtNanos;
        this.waitingForServerResult = false;
        this.winnerRequestSent = false;
        this.timeResultsPending = false;
        this.timeResultsSent = false;
        this.matchResultPacket = null;
        this.eliminationCounter = 0;
        chatArea.setText("");
        java.util.Arrays.fill(lastReportedLines, 0);
        java.util.Arrays.fill(reportedScores, 0);
        java.util.Arrays.fill(reportedLines, 0);
        java.util.Arrays.fill(eliminationOrder, 0);
        java.util.Arrays.fill(playerNames, "");
        if (nicknames != null) {
            System.arraycopy(nicknames, 0, playerNames, 0, Math.min(nicknames.length, playerNames.length));
        }
        modeStatus.setText(mode.getLabel());

        gridPanel.setLayout(new ResponsiveGridLayout(4, 1, 220, PANEL_GAP, 0));

        for (int i = 0; i < 4; i++) {
            slotTypes[i] = slots[i];
            isGameOver[i] = (slots[i] == SlotType.EMPTY || slots[i] == SlotType.CLOSED);

            if (slots[i] == SlotType.EMPTY || slots[i] == SlotType.CLOSED) continue;

            InputSourceType inputType = InputSourceType.Keyboard2;

            if (i == mySlotIndex) {
                inputType = InputSourceType.Keyboard1;
            } else if (isHost && slots[i] == SlotType.BOT) {
                inputType = InputSourceType.AI;
            }

            Tetris game = new Tetris(inputType, new TetrominoQueue(new PieceGenerator_7Bag()), level, settings);
            game.flow.setSystemControlsEnabled(false);
            allGames.put(i, game);

            if (i == mySlotIndex || (isHost && slots[i] == SlotType.BOT)) {
                localGames.put(i, game);
            }

            MultiGameBoardPanel panel = new MultiGameBoardPanel(game);
            gamePanels.put(i, panel);

            String name = playerNames[i] == null ? "" : playerNames[i].trim();
            if (name.isEmpty()) {
                name = slots[i] == SlotType.BOT ? "AI 봇 " + i : (i == mySlotIndex ? "나" : (i + 1) + "P 플레이어");
            } else if (i == mySlotIndex) {
                name += " (나)";
            }
            gridPanel.add(playerColumn(name, panel, i));
        }

        setupNetworkListener();

        updateStandings();
        gridPanel.revalidate();
        gridPanel.repaint();
        requestFocusInWindow();
        timer.start();
    }

    private void setupNetworkListener() {
        if (networkListener != null) NetworkManager.getInstance().removePacketListener(networkListener);
        networkListener = packet -> {
            String[] tokens = packet.split("\\|", -1);
            String cmd = tokens[0];

            SwingUtilities.invokeLater(() -> {
                if ("RES_GAME_OVER".equals(cmd) && tokens.length >= 2) {
                    try {
                        int slot = Integer.parseInt(tokens[1]);
                        if (slot >= 0 && slot < isGameOver.length) {
                            if (!isGameOver[slot]) {
                                isGameOver[slot] = true;
                                eliminationOrder[slot] = ++eliminationCounter;
                                updateStandings();
                            }
                            Tetris game = allGames.get(slot);
                            if (game != null) game.flow.markGameOver();
                        }
                    } catch (Exception ignored) {}
                }
                else if ("RES_PLAYER_STATS".equals(cmd) && tokens.length >= 4) {
                    try {
                        int slot = Integer.parseInt(tokens[1]);
                        int score = Integer.parseInt(tokens[2]);
                        int lines = Integer.parseInt(tokens[3]);
                        if (slot >= 0 && slot < slotTypes.length && score >= 0 && lines >= 0
                                && (slotTypes[slot] == SlotType.HUMAN || slotTypes[slot] == SlotType.BOT)) {
                            reportedScores[slot] = score;
                            reportedLines[slot] = lines;
                            updateStandings();
                        }
                    } catch (NumberFormatException ignored) {}
                }

                else if ("RES_BOARD_SYNC".equals(cmd) && tokens.length >= 3) {
                    try {
                        int slot = Integer.parseInt(tokens[1]);
                        String payload = tokens[2]; // 보드문자열

                        if (slot >= 0 && slot < slotTypes.length
                                && slot != mySlotIndex && (!isHost || slotTypes[slot] != SlotType.BOT)) {
                            Tetris remoteGame = allGames.get(slot);
                            if (remoteGame != null) {
                                BoardSnapshotCodec.apply(remoteGame.board, remoteGame.activePiece, payload);
                                gamePanels.get(slot).repaint();
                            }
                        }
                    } catch (Exception ignored) {}
                }
                else if ("RES_MATCH_RESULT".equals(cmd) && tokens.length >= 3) {
                    try {
                        Integer.parseInt(tokens[1]);
                        matchResultPacket = packet;
                        waitingForServerResult = false;
                        finished = true;
                        finishedFrames = 0;
                        NetworkManager.getInstance().requestUserInfo();
                    } catch (Exception ignored) {}
                }
                else if ("RES_CHAT".equals(cmd) && tokens.length >= 3) {
                    appendChat(tokens[1], tokens[2]);
                }
            });
        };
        NetworkManager.getInstance().addPacketListener(networkListener);
    }

    private void tick() {
        if (System.nanoTime() < countdownEndsAtNanos) {
            repaint();
            return;
        }
        if (timeResultsPending && !timeResultsSent && System.nanoTime() >= timeResultsSendAtNanos) {
            for (Map.Entry<Integer, Tetris> entry : localGames.entrySet()) {
                int slot = entry.getKey();
                if (!isGameOver[slot]) sendTimeResult(slot, entry.getValue());
            }
            timeResultsSent = true;
            return;
        }
        if (finished) {
            if (!waitingForServerResult && ++finishedFrames >= GAME_OVER_HOLD_FRAMES) reportResult();
            return;
        }

        long elapsedNanos = System.nanoTime() - roundStartedAtNanos;
        if (mode == MultiGameMode.TIME_ATTACK && elapsedNanos >= mode.getTimeLimitSec() * 1_000_000_000L) {
            timeResultsPending = true;
            timeResultsSendAtNanos = System.nanoTime() + TIME_RESULT_GRACE_NANOS;
            waitingForServerResult = true;
            finished = true;
            modeStatus.setText("시간 종료 · 결과 집계 중");
            return;
        }
        if (mode == MultiGameMode.TIME_ATTACK) {
            long remaining = Math.max(0, mode.getTimeLimitSec() - elapsedNanos / 1_000_000_000L);
            modeStatus.setText("시간전 · 남은 시간 " + remaining + "초");
        } else {
            modeStatus.setText(mode.getLabel());
        }

        for (Map.Entry<Integer, Tetris> entry : localGames.entrySet()) {
            int slot = entry.getKey();
            Tetris game = entry.getValue();

            if (isGameOver[slot]) continue;

            game.tick();

            int lines = game.flow.getLines();
            if (lines > lastReportedLines[slot]) {
                lastReportedLines[slot] = lines;
            }

            if (syncTick % 3 == 0) {
                sendBoardSync(slot, game);
            }
            if (statsSyncTick % 15 == 0) {
                int score = game.flow.getScore();
                int currentLines = game.flow.getLines();
                reportedScores[slot] = score;
                reportedLines[slot] = currentLines;
                updateStandings();
                NetworkManager.getInstance().reportPlayerStats(slot,
                        score, currentLines);
            }

            if (game.flow.isGameOver()) {
                if (!isGameOver[slot]) {
                    isGameOver[slot] = true;
                    eliminationOrder[slot] = ++eliminationCounter;
                    updateStandings();
                }
                NetworkManager.getInstance().reportGameOver(slot,
                        game.flow.getScore(), game.flow.getLines());
            } else if (mode == MultiGameMode.CLASSIC_40 && lines >= mode.getTargetLines()
                    && !winnerRequestSent) {
                winnerRequestSent = true;
                waitingForServerResult = true;
                finished = true;
                NetworkManager.getInstance().reportMatchWin(slot, game.flow.getScore(), lines);
            }
        }

        syncTick++;
        statsSyncTick++;
        checkMultiGameFinished();
        repaint();
    }

    // 내 보드의 고정된 블록과 떨어지는 블록 정보를 조합하여 문자열로 압축 발송
    private void sendBoardSync(int slot, Tetris game) {
        String snapshot = BoardSnapshotCodec.encode(game.board, game.activePiece);
        NetworkManager.getInstance().sendBoardSync(slot, snapshot);
    }

    private void checkMultiGameFinished() {
        int totalPlayers = 0;
        int activeCount = 0;

        for (int i = 0; i < 4; i++) {
            if (slotTypes[i] == SlotType.HUMAN || slotTypes[i] == SlotType.BOT) {
                totalPlayers++;
                if (!isGameOver[i]) activeCount++;
            }
        }

        if (mode != MultiGameMode.TIME_ATTACK
                && ((totalPlayers > 1 && activeCount <= 1) || (totalPlayers == 1 && activeCount == 0))) {
            waitingForServerResult = true;
            finished = true;
            modeStatus.setText("결과 확인 중");
        }
    }

    private void sendTimeResult(int slot, Tetris game) {
        NetworkManager.getInstance().reportTimeResult(slot,
                game.flow.getScore(), game.flow.getLines());
    }

    private void reportResult() {
        stop();
        if (onFinished != null && matchResultPacket != null) onFinished.accept(matchResultPacket);
    }

    public void stop() {
        timer.stop();
        if (networkListener != null) {
            NetworkManager.getInstance().removePacketListener(networkListener);
            networkListener = null;
        }
        for (Tetris game : allGames.values()) game.close();
        allGames.clear();
        localGames.clear();
        gamePanels.clear();
        gridPanel.removeAll();
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(4, 4));
        panel.setPreferredSize(new Dimension(250, 0));
        panel.setMinimumSize(new Dimension(220, 0));
        panel.setBackground(UIUtils.COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(6, 8, 8, 8)));
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);
        chatArea.setBackground(UIUtils.COLOR_BACKGROUND);
        chatArea.setForeground(Color.WHITE);
        chatArea.setFont(UIUtils.getFont(Font.PLAIN, 13f));
        JScrollPane scroll = new JScrollPane(chatArea);
        scroll.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        chatField.addActionListener(e -> sendChatMessage());
        chatField.setBackground(UIUtils.COLOR_SURFACE_RAISED);
        chatField.setForeground(UIUtils.COLOR_TEXT);
        chatField.setCaretColor(UIUtils.COLOR_ACCENT);
        chatField.setFont(UIUtils.getFont(Font.PLAIN, 13f));
        JButton send = UIUtils.createSecondaryButton("전송");
        send.setPreferredSize(new Dimension(72, 36));
        send.addActionListener(e -> sendChatMessage());
        JPanel input = new JPanel(new BorderLayout(4, 0));
        input.setOpaque(false);
        input.add(chatField, BorderLayout.CENTER);
        input.add(send, BorderLayout.EAST);
        panel.add(createStandingsPanel(), BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(input, BorderLayout.SOUTH);
        return panel;
    }

    private void updateStandings() {
        List<Integer> participants = new ArrayList<>();
        for (int slot = 0; slot < slotTypes.length; slot++) {
            if (slotTypes[slot] == SlotType.HUMAN || slotTypes[slot] == SlotType.BOT) participants.add(slot);
        }
        participants.sort(this::compareStandingSlots);
        standingsRows.removeAll();
        for (int index = 0; index < participants.size(); index++) {
            int slot = participants.get(index);
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setBorder(BorderFactory.createEmptyBorder(2, 3, 2, 3));
            JLabel rank = new JLabel((index + 1) + "위", SwingConstants.LEFT);
            rank.setForeground(index == 0 ? new Color(255, 207, 82) : UIUtils.COLOR_MUTED);
            rank.setFont(UIUtils.getFont(Font.BOLD, 12f));
            String name = playerNames[slot] == null || playerNames[slot].trim().isEmpty()
                    ? (slotTypes[slot] == SlotType.BOT ? "AI 봇 " + slot : (slot + 1) + "P 플레이어")
                    : playerNames[slot];
            JLabel player = new JLabel(name + (isGameOver[slot] ? " · 탈락" : ""));
            player.setForeground(isGameOver[slot] ? UIUtils.COLOR_MUTED : UIUtils.COLOR_TEXT);
            player.setFont(UIUtils.getBodyFont(Font.PLAIN, 12f));
            JLabel score = new JLabel(String.format("%,d점 · %d줄", reportedScores[slot], reportedLines[slot]),
                    SwingConstants.RIGHT);
            score.setForeground(UIUtils.COLOR_TEXT);
            score.setFont(UIUtils.getBodyFont(Font.BOLD, 11f));
            JPanel identity = new JPanel(new BorderLayout(6, 0));
            identity.setOpaque(false);
            identity.add(rank, BorderLayout.WEST);
            identity.add(player, BorderLayout.CENTER);
            row.add(identity, BorderLayout.CENTER);
            row.add(score, BorderLayout.EAST);
            standingsRows.add(row);
        }
        standingsRows.revalidate();
        standingsRows.repaint();
    }

    private int compareStandingSlots(int first, int second) {
        if (isGameOver[first] != isGameOver[second]) return isGameOver[first] ? 1 : -1;
        if (isGameOver[first] && eliminationOrder[first] != eliminationOrder[second]) {
            return Integer.compare(eliminationOrder[second], eliminationOrder[first]);
        }
        int primary = Integer.compare(reportedLines[second], reportedLines[first]);
        if (primary != 0) return primary;
        int score = Integer.compare(reportedScores[second], reportedScores[first]);
        if (score != 0) return score;
        int lines = Integer.compare(reportedLines[second], reportedLines[first]);
        return lines != 0 ? lines : Integer.compare(first, second);
    }

    private JPanel createStandingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 3));
        panel.setBackground(UIUtils.COLOR_BACKGROUND);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(5, 6, 5, 6)));
        JLabel heading = new JLabel("실시간 순위");
        heading.setForeground(UIUtils.COLOR_ACCENT);
        heading.setFont(UIUtils.getFont(Font.BOLD, 12f));
        standingsRows.setLayout(new BoxLayout(standingsRows, BoxLayout.Y_AXIS));
        standingsRows.setOpaque(false);
        panel.add(heading, BorderLayout.NORTH);
        panel.add(standingsRows, BorderLayout.CENTER);
        return panel;
    }

    private void sendChatMessage() {
        String message = chatField.getText().trim();
        if (message.isEmpty()) return;
        StringBuilder safe = new StringBuilder(Math.min(200, message.length()));
        for (int i = 0; i < message.length() && safe.length() < 200; i++) {
            char ch = message.charAt(i);
            if (!Character.isISOControl(ch) && ch != '|') safe.append(ch);
        }
        if (safe.length() > 0) NetworkManager.getInstance().sendChat("ROOM", safe.toString());
        chatField.setText("");
    }

    private void appendChat(String sender, String message) {
        chatArea.append("[" + sender + "] " + message + "\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    private void leaveGame() {
        if (onLeaveRequested != null) {
            onLeaveRequested.run();
            return;
        }
        int choice = JOptionPane.showConfirmDialog(this, "진행 중인 게임에서 나갈까요?", "게임 나가기",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (choice != JOptionPane.YES_OPTION) return;
        confirmLeaveGame();
    }

    private void toggleChatPanel() {
        boolean show = !chatPanel.isVisible();
        chatPanel.setVisible(show);
        chatToggleButton.setText(show ? "채팅 숨기기" : "채팅 보기");
        playArea.revalidate();
        playArea.repaint();
    }

    public void confirmLeaveGame() {
        for (Map.Entry<Integer, Tetris> entry : localGames.entrySet()) {
            int slot = entry.getKey();
            Tetris game = entry.getValue();
            if (!isGameOver[slot]) {
                NetworkManager.getInstance().reportGameOver(slot,
                        game.flow.getScore(), game.flow.getLines());
            }
        }
        NetworkManager.getInstance().leaveRoom();
        stop();
        if (onFinished != null) onFinished.accept("LEFT_GAME");
    }

    private JComponent playerColumn(String name, MultiGameBoardPanel panel, int slot) {
        JPanel column = new JPanel(new BorderLayout(0, 2)) {
            @Override
            protected void paintChildren(Graphics graphics) {
                super.paintChildren(graphics);
                if (!isGameOver[slot]) return;
                Graphics2D g = (Graphics2D) graphics.create();
                g.setColor(new Color(18, 20, 26, 175));
                g.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g.setColor(new Color(220, 225, 235));
                g.setFont(UIUtils.getFont(Font.BOLD, 22f));
                String label = "탈락";
                FontMetrics metrics = g.getFontMetrics();
                g.drawString(label, (getWidth() - metrics.stringWidth(label)) / 2,
                        (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
                g.dispose();
            }
        };
        column.setOpaque(true);
        column.setBackground(UIUtils.COLOR_SURFACE);
        column.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        JLabel label = new JLabel(name, SwingConstants.CENTER);
        label.setForeground(TEXT);
        label.setFont(UIUtils.getFont(Font.BOLD, 15f));
        column.add(label, BorderLayout.NORTH);
        column.add(panel, BorderLayout.CENTER);
        column.setMinimumSize(new Dimension(1, 1));
        return column;
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        long remainingNanos = countdownEndsAtNanos - System.nanoTime();
        if (remainingNanos <= 0 || allGames.isEmpty()) return;
        int count = (int) Math.ceil(remainingNanos / 1_000_000_000.0);
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int box = Math.min(190, Math.max(130, Math.min(getWidth(), getHeight()) / 3));
        int x = (getWidth() - box) / 2;
        int y = (getHeight() - box) / 2;
        g.setColor(new Color(8, 10, 16, 205));
        g.fillRoundRect(x, y, box, box, 36, 36);
        g.setColor(new Color(255, 205, 90));
        g.setStroke(new BasicStroke(3f));
        g.drawRoundRect(x, y, box, box, 36, 36);
        g.setFont(UIUtils.getFont(Font.BOLD, box * 0.56f));
        String text = Integer.toString(count);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x + (box - metrics.stringWidth(text)) / 2,
                y + (box - metrics.getHeight()) / 2 + metrics.getAscent());
        g.dispose();
    }
}
