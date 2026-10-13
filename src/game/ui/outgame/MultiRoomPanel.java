package game.ui.outgame;

import game.NetworkManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import game.ui.common.ResponsiveGridLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

//======================================================================================================================
// 멀티플레이 방
//======================================================================================================================
public class MultiRoomPanel extends JPanel {
    private MainFrame mainFrame;

    private String roomTitle = "테트리스 방";
    private String gameModeStr = "시간전 (90초)";
    private int mySlotIndex = 0;
    private boolean isHost = false;
    private int hostSlotIndex = 0;
    private int currentRoomId = -1;

    public enum SlotType { HUMAN, BOT, EMPTY, CLOSED }
    private SlotType[] slotTypes = new SlotType[]{SlotType.HUMAN, SlotType.EMPTY, SlotType.EMPTY, SlotType.EMPTY};
    private boolean[] slotReady = new boolean[]{true, false, false, false};
    private String[] slotSkins = new String[]{"WHITE", "WHITE", "WHITE", "WHITE"};
    private String[] slotNicks = new String[]{"", "", "", ""};

    private JLabel titleLabel;
    private JLabel modeLabel;
    private JLabel countLabel;
    private JButton actionBtn;

    private JTextArea chatArea;
    private JTextField chatField;

    private JPanel[] slotCards = new JPanel[4];
    private JLabel[] nickLabels = new JLabel[4];
    private JLabel[] statusLabels = new JLabel[4];
    private JLabel[] centerIcons = new JLabel[4];

    private String centerNotice;
    private long centerNoticeUntil;
    private final Timer noticeTimer;

    public MultiRoomPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new BorderLayout(15, 15));
        setBackground(new Color(24, 26, 32));
        setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));
        noticeTimer = new Timer(80, e -> {
            if (System.currentTimeMillis() >= centerNoticeUntil) {
                centerNotice = null;
                ((Timer) e.getSource()).stop();
            }
            repaint();
        });

        // 1. 상단 바
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setOpaque(false);
        topPanel.setPreferredSize(new Dimension(0, 44));

        JButton leaveBtn = UIUtils.createDangerButton("방 나가기");
        leaveBtn.setPreferredSize(new Dimension(128, 42));
        leaveBtn.setMaximumSize(new Dimension(128, 42));
        leaveBtn.addActionListener(e -> handleLeaveRoom());

        JButton settingsBtn = UIUtils.createSecondaryButton("설정");
        settingsBtn.setPreferredSize(new Dimension(92, 40));
        settingsBtn.addActionListener(e -> mainFrame.showSettingsPopup());

        JPanel titleArea = new JPanel(new BorderLayout());
        titleArea.setOpaque(false);
        titleLabel = UIUtils.createLabel(roomTitle, 24f, Color.WHITE);
        titleLabel.setToolTipText("방장이면 제목을 눌러 수정할 수 있습니다.");
        titleArea.add(createEditableGroup(titleLabel, this::editRoomTitle), BorderLayout.CENTER);

        topPanel.add(leaveBtn, BorderLayout.WEST);
        topPanel.add(titleArea, BorderLayout.CENTER);
        topPanel.add(settingsBtn, BorderLayout.EAST);

        // 2. 중앙 4슬롯 그리드 + 우측 채팅창
        JPanel mainCenterPanel = new JPanel(new BorderLayout(15, 0));
        mainCenterPanel.setOpaque(false);

        JPanel slotsPanel = new JPanel(new ResponsiveGridLayout(4, 160, 150, 12, 12));
        slotsPanel.setOpaque(false);
        for (int i = 0; i < 4; i++) {
            slotsPanel.add(createPlayerCard(i));
        }

        JPanel chatPanel = createChatPanel();
        mainCenterPanel.add(slotsPanel, BorderLayout.CENTER);
        mainCenterPanel.add(chatPanel, BorderLayout.EAST);
        mainCenterPanel.addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override public void componentResized(java.awt.event.ComponentEvent e) {
                int width = Math.max(210, Math.min(280, mainCenterPanel.getWidth() / 3));
                if (chatPanel.getPreferredSize().width != width) {
                    chatPanel.setPreferredSize(new Dimension(width, 0));
                    mainCenterPanel.revalidate();
                }
            }
        });

        // 3. 하단 컨트롤 바
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);

        JPanel infoPanel = new JPanel(new GridLayout(2, 1, 3, 3));
        infoPanel.setOpaque(false);
        modeLabel = UIUtils.createLabel("게임 모드: " + gameModeStr, 15f, Color.LIGHT_GRAY);
        countLabel = UIUtils.createLabel("참여 중인 인원", 13f, Color.GRAY);
        JPanel modeGroup = createEditableGroup(modeLabel, this::editGameMode);
        infoPanel.add(modeGroup);
        infoPanel.add(countLabel);

        actionBtn = UIUtils.createStyledButton("준비하기");
        actionBtn.setFont(actionBtn.getFont().deriveFont(Font.BOLD, 18f));
        actionBtn.setPreferredSize(new Dimension(160, 50));
        actionBtn.addActionListener(e -> handleActionClick());

        bottomPanel.add(infoPanel, BorderLayout.WEST);
        bottomPanel.add(actionBtn, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);
        add(mainCenterPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        setupNetworkListener();
    }

    private void setupNetworkListener() {
        Consumer<String> roomPacketListener = packet -> {
            if (packet == null || packet.trim().isEmpty()) return;
            String[] tokens = packet.split("\\|", -1);
            String cmd = tokens[0];

            SwingUtilities.invokeLater(() -> {
                if ("RES_CHAT".equals(cmd) && tokens.length >= 3) {
                    int messageStart = packet.indexOf('|', packet.indexOf('|') + 1) + 1;
                    String message = messageStart > 0 ? packet.substring(messageStart) : "";
                    if ("SYSTEM".equals(tokens[1]) && isStartRequirementNotice(message)) {
                        showCenterNotice(message);
                    } else {
                        appendChatMessage(tokens[1], message);
                    }
                } else if ("RES_ROOM_INFO".equals(cmd)) {
                    updateRoomInfo(tokens);
                } else if ("RES_READY_TOGGLE".equals(cmd) && tokens.length >= 3) {
                    try {
                        int slotIdx = Integer.parseInt(tokens[1]);
                        if (slotIdx < 0 || slotIdx >= slotReady.length) return;
                        boolean ready = Boolean.parseBoolean(tokens[2]);
                        slotReady[slotIdx] = ready;
                        updateSlotUI(slotIdx);
                    } catch (Exception ignored) {}
                } else if ("RES_START_GAME".equals(cmd)) {
                    if (mainFrame != null) {
                        mainFrame.startMultiGame(mySlotIndex, isHost, slotTypes, slotNicks, gameModeStr);
                    }
                } else if ("RES_JOIN_ROOM".equals(cmd) && tokens.length >= 2 && "SUCCESS".equals(tokens[1])) {
                    if (tokens.length >= 4) {
                        clearRoomChat(parseInt(tokens[2], -1));
                        mySlotIndex = Integer.parseInt(tokens[3]);
                        isHost = (mySlotIndex == hostSlotIndex);
                        updateActionBtnText();
                        // 🌟 방 입장 성공 시 대기실 방 화면으로 전환!
                        if (mainFrame != null) {
                            mainFrame.showScreen(MainFrame.TAG_MULTI_ROOM);
                        }
                    }
                } else if ("RES_CREATE_ROOM".equals(cmd) && tokens.length >= 2 && "SUCCESS".equals(tokens[1])) {
                    clearRoomChat(tokens.length >= 3 ? parseInt(tokens[2], -1) : -1);
                    mySlotIndex = 0;
                    isHost = true;
                    updateActionBtnText();
                    // 🌟 방 생성 성공 시 대기실 방 화면으로 전환!
                    if (mainFrame != null) {
                        mainFrame.showScreen(MainFrame.TAG_MULTI_ROOM);
                    }
                } else if (("RES_JOIN_ROOM".equals(cmd) || "RES_CREATE_ROOM".equals(cmd))
                        && tokens.length >= 3 && "FAIL".equals(tokens[1])) {
                    JOptionPane.showMessageDialog(this, tokens[2], "방 입장 오류", JOptionPane.WARNING_MESSAGE);
                }
            });
        };
        NetworkManager.getInstance().addPacketListener(roomPacketListener);
    }

    /** 서버 방 정보 패킷에서 호스트와 네 슬롯의 참가 상태를 갱신합니다. */
    private void updateRoomInfo(String[] tokens) {
        if (tokens.length < 5) return;
        clearRoomChat(parseInt(tokens[1], -1));
        String hostNickname = tokens[4];
        this.roomTitle = tokens[2];
        this.gameModeStr = tokens[3];
        titleLabel.setText(roomTitle);
        modeLabel.setText("게임 모드: " + gameModeStr);

        int idx = 5;
        for (int i = 0; i < 4; i++) {
            if (idx + 3 < tokens.length) {
                try {
                    slotTypes[i] = SlotType.valueOf(tokens[idx]);
                    slotNicks[i] = tokens[idx + 1];
                    slotReady[i] = Boolean.parseBoolean(tokens[idx + 2]);
                    slotSkins[i] = tokens[idx + 3];
                } catch (Exception e) {
                    slotTypes[i] = SlotType.EMPTY;
                }
                idx += 4;
            }
            updateSlotUI(i);
        }

        hostSlotIndex = 0;
        if (tokens.length >= 23 && "HOST_SLOT".equals(tokens[21])) {
            try {
                int parsedHostSlot = Integer.parseInt(tokens[22]);
                if (parsedHostSlot >= 0 && parsedHostSlot < slotNicks.length) hostSlotIndex = parsedHostSlot;
            } catch (NumberFormatException ignored) {}
        } else {
            for (int i = 0; i < slotNicks.length; i++) {
                if (slotTypes[i] == SlotType.HUMAN && hostNickname.equals(slotNicks[i])) {
                    hostSlotIndex = i;
                    break;
                }
            }
        }
        int participantCount = 0;
        for (SlotType type : slotTypes) if (type == SlotType.HUMAN || type == SlotType.BOT) participantCount++;
        countLabel.setText("참가 인원 " + participantCount + " / 4");
        this.isHost = mySlotIndex == hostSlotIndex;
        for (int i = 0; i < slotTypes.length; i++) updateSlotUI(i);
        updateActionBtnText();
    }

    private void updateSlotUI(int i) {
        SlotType type = slotTypes[i];
        if (type == SlotType.HUMAN) {
            nickLabels[i].setText(slotNicks[i].isEmpty() ? "플레이어" : slotNicks[i]);
            nickLabels[i].setForeground(Color.WHITE);
            statusLabels[i].setText(i == hostSlotIndex ? "방장 · " + (slotReady[i] ? "준비완료" : "준비중")
                    : (slotReady[i] ? "준비완료" : "준비중"));
            statusLabels[i].setForeground(slotReady[i] ? new Color(112, 235, 164) : new Color(255, 205, 105));
            centerIcons[i].setVisible(false);
        } else if (type == SlotType.BOT) {
            nickLabels[i].setText("AI 봇 " + i);
            nickLabels[i].setForeground(new Color(180, 220, 255));
            statusLabels[i].setText("AI BOT · 쉬움");
            statusLabels[i].setForeground(Color.GREEN);
            centerIcons[i].setFont(UIUtils.getBodyFont(Font.BOLD, 50f));
            centerIcons[i].setForeground(new Color(180, 220, 255));
            centerIcons[i].setText("AI");
            centerIcons[i].setVisible(true);
        } else if (type == SlotType.CLOSED) {
            nickLabels[i].setText("잠긴 슬롯");
            statusLabels[i].setText("입장 불가");
            statusLabels[i].setForeground(new Color(255, 164, 175));
            centerIcons[i].setFont(UIUtils.getBodyFont(Font.BOLD, 17f));
            centerIcons[i].setForeground(new Color(255, 164, 175));
            centerIcons[i].setText("잠금");
            centerIcons[i].setVisible(true);
        } else { // EMPTY
            nickLabels[i].setText("빈 자리");
            statusLabels[i].setText("");
            centerIcons[i].setFont(UIUtils.getBodyFont(Font.PLAIN, 50f));
            centerIcons[i].setForeground(Color.GRAY);
            centerIcons[i].setText("+");
            centerIcons[i].setVisible(true);
        }
        nickLabels[i].setFont(UIUtils.getBodyFont(Font.BOLD, 14f));
        statusLabels[i].setFont(UIUtils.getBodyFont(Font.BOLD, 12f));
        slotCards[i].setBackground(slotBackground(slotSkins[i], type));
        slotCards[i].setToolTipText(isHost && (type == SlotType.EMPTY || type == SlotType.CLOSED)
                ? (type == SlotType.EMPTY ? "클릭하여 봇을 추가하거나 슬롯을 잠급니다." : "클릭하여 슬롯을 다시 엽니다.")
                : null);
        slotCards[i].setOpaque(true);
        slotCards[i].setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(slotAccent(slotSkins[i], type), type == SlotType.HUMAN ? 3 : 1),
                BorderFactory.createEmptyBorder(8, 6, 8, 6)));
        slotCards[i].repaint();
    }

    private void updateActionBtnText() {
        if (isHost) {
            actionBtn.setText("플레이");
        } else {
            actionBtn.setText(slotReady[mySlotIndex] ? "레디 해제" : "준비하기");
        }
    }

    private JPanel createPlayerCard(int slotIdx) {
        JPanel card = new JPanel(new BorderLayout());
        card.setOpaque(true);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        JLabel centerIcon = new JLabel("+", SwingConstants.CENTER);
        centerIcon.setFont(UIUtils.getBodyFont(Font.PLAIN, 50f));
        centerIcon.setForeground(Color.GRAY);
        centerIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        centerIcons[slotIdx] = centerIcon;

        JLabel nick = UIUtils.createLabel("빈 자리", 14f, Color.GRAY);
        nick.setAlignmentX(Component.CENTER_ALIGNMENT);
        nickLabels[slotIdx] = nick;

        JLabel status = UIUtils.createLabel("", 12f, Color.YELLOW);
        status.setAlignmentX(Component.CENTER_ALIGNMENT);
        statusLabels[slotIdx] = status;

        contentPanel.add(Box.createVerticalGlue());
        contentPanel.add(centerIcon);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(nick);
        contentPanel.add(status);
        contentPanel.add(Box.createVerticalGlue());

        card.add(contentPanel, BorderLayout.CENTER);
        slotCards[slotIdx] = card;

        setupSlotClickMenu(card, slotIdx);
        return card;
    }

    private void setupSlotClickMenu(JPanel card, int slotIdx) {
        MouseAdapter slotMenuListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!isHost || slotIdx == mySlotIndex) return;
                JPopupMenu popup = new JPopupMenu();

                if (slotTypes[slotIdx] == SlotType.EMPTY) {
                    JMenuItem addBot = new JMenuItem("AI 봇 추가");
                    addBot.addActionListener(evt -> NetworkManager.getInstance().addBot(slotIdx));
                    popup.add(addBot);
                    JMenuItem lockSlot = new JMenuItem("슬롯 잠그기");
                    lockSlot.addActionListener(evt -> NetworkManager.getInstance().setSlotLocked(slotIdx, true));
                    popup.add(lockSlot);
                } else if (slotTypes[slotIdx] == SlotType.BOT) {
                    JMenuItem removeBot = new JMenuItem("AI 봇 제거");
                    removeBot.addActionListener(evt -> NetworkManager.getInstance().removeBot(slotIdx));
                    popup.add(removeBot);
                } else if (slotTypes[slotIdx] == SlotType.CLOSED) {
                    JMenuItem unlockSlot = new JMenuItem("슬롯 열기");
                    unlockSlot.addActionListener(evt -> NetworkManager.getInstance().setSlotLocked(slotIdx, false));
                    popup.add(unlockSlot);
                } else {
                    return;
                }
                Component source = (Component) e.getSource();
                Point location = SwingUtilities.convertPoint(source, e.getPoint(), card);
                popup.show(card, location.x, location.y);
            }
        };
        addSlotListenerRecursively(card, slotMenuListener);
    }

    private static void addSlotListenerRecursively(Component component, MouseAdapter listener) {
        component.addMouseListener(listener);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                addSlotListenerRecursively(child, listener);
            }
        }
    }

    private void handleActionClick() {
        if (isHost) {
            for (int i = 0; i < slotTypes.length; i++) {
                if (slotTypes[i] == SlotType.HUMAN && i != hostSlotIndex && !slotReady[i]) {
                    showCenterNotice("모두가 준비해야 시작할 수 있습니다");
                    return;
                }
            }
            NetworkManager.getInstance().startGame();
        } else {
            NetworkManager.getInstance().toggleReady();
        }
    }

    private void handleLeaveRoom() {
        mainFrame.showRoomLeavePopup(() -> {
            NetworkManager.getInstance().leaveRoom();
            clearRoomChat(-1);
            mainFrame.showScreen(MainFrame.TAG_MULTI_LOBBY);
        });
    }

    private JPanel createChatPanel() {
        JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(UIUtils.COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)));

        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setBackground(UIUtils.COLOR_BACKGROUND);
        chatArea.setForeground(UIUtils.COLOR_TEXT);
        chatArea.setFont(UIUtils.getFont(Font.PLAIN, 13f));
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        JScrollPane scrollPane = new JScrollPane(chatArea);
        scrollPane.setBorder(BorderFactory.createLineBorder(UIUtils.COLOR_BORDER));
        chatField = new JTextField();
        chatField.setBackground(UIUtils.COLOR_SURFACE_RAISED);
        chatField.setForeground(UIUtils.COLOR_TEXT);
        chatField.setCaretColor(UIUtils.COLOR_ACCENT);
        chatField.setFont(UIUtils.getFont(Font.PLAIN, 13f));
        chatField.addActionListener(e -> sendChatMessage());

        JButton sendBtn = UIUtils.createSecondaryButton("전송");
        sendBtn.setPreferredSize(new Dimension(72, 36));
        sendBtn.addActionListener(e -> sendChatMessage());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.add(chatField, BorderLayout.CENTER);
        inputPanel.add(sendBtn, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputPanel, BorderLayout.SOUTH);
        return panel;
    }

    private void sendChatMessage() {
        String msg = chatField.getText().trim();
        if (!msg.isEmpty()) {
            StringBuilder safe = new StringBuilder(Math.min(200, msg.length()));
            for (int i = 0; i < msg.length() && safe.length() < 200; i++) {
                char ch = msg.charAt(i);
                if (!Character.isISOControl(ch)) safe.append(ch);
            }
            if (safe.length() > 0) NetworkManager.getInstance().sendChat("ROOM", safe.toString());
            chatField.setText("");
        }
    }

    public void appendChatMessage(String sender, String msg) {
        if (chatArea != null && currentRoomId >= 0) {
            chatArea.append("[" + sender + "] " + msg + "\n");
            chatArea.setCaretPosition(chatArea.getDocument().getLength());
        }
    }

    private JPanel createEditableGroup(JLabel label, Runnable editAction) {
        JPanel group = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        group.setOpaque(false);
        JButton pencil = createPencilButton(editAction);
        pencil.setVisible(true);
        pencil.setIcon(new EmptyIcon(18, 18));
        group.add(label);
        group.add(pencil);
        MouseAdapter hover = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                pencil.setIcon(isHost ? new PencilIcon() : new EmptyIcon(18, 18));
            }
            @Override public void mouseExited(MouseEvent e) {
                SwingUtilities.invokeLater(() -> {
                    Point position = group.getMousePosition(true);
                    if (isHost && position != null) return;
                    pencil.setIcon(new EmptyIcon(18, 18));
                });
            }
        };
        group.addMouseListener(hover);
        label.addMouseListener(hover);
        pencil.addMouseListener(hover);
        label.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { if (isHost) editAction.run(); }
        });
        label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        group.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return group;
    }

    private JButton createPencilButton(Runnable editAction) {
        JButton button = new JButton(new PencilIcon());
        button.setToolTipText("수정");
        button.setPreferredSize(new Dimension(24, 24));
        button.setBorder(BorderFactory.createEmptyBorder(3, 3, 3, 3));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.addActionListener(e -> { if (isHost) editAction.run(); });
        return button;
    }

    private static final class EmptyIcon implements Icon {
        private final int width;
        private final int height;
        private EmptyIcon(int width, int height) { this.width = width; this.height = height; }
        @Override public int getIconWidth() { return width; }
        @Override public int getIconHeight() { return height; }
        @Override public void paintIcon(Component component, Graphics graphics, int x, int y) { }
    }

    private void editRoomTitle() {
        JTextField field = new JTextField(roomTitle, 20);
        field.setFont(UIUtils.getBodyFont(Font.PLAIN, 14f));
        int result = JOptionPane.showConfirmDialog(this, field, "방 제목 수정",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result != JOptionPane.OK_OPTION) return;
        String title = field.getText().trim();
        if (!isValidRoomTitle(title)) {
            JOptionPane.showMessageDialog(this, "방 제목은 1~30자이며 쉼표와 구분자를 사용할 수 없습니다.",
                    "잘못된 제목", JOptionPane.WARNING_MESSAGE);
            return;
        }
        NetworkManager.getInstance().changeRoomTitle(title);
    }

    private void editGameMode() {
        String[] modes = new String[]{"시간전 (90초)", "클래식 (40줄)", "아이템전"};
        JComboBox<String> selector = new JComboBox<>(modes);
        selector.setSelectedItem(gameModeStr);
        selector.setFont(UIUtils.getBodyFont(Font.PLAIN, 14f));
        int result = JOptionPane.showConfirmDialog(this, selector, "게임 모드 수정",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (result == JOptionPane.OK_OPTION) {
            NetworkManager.getInstance().changeRoomSettings(String.valueOf(selector.getSelectedItem()));
        }
    }

    private static final class PencilIcon implements Icon {
        @Override public int getIconWidth() { return 18; }
        @Override public int getIconHeight() { return 18; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(206, 216, 234));
            g.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawLine(x + 4, y + 14, x + 13, y + 5);
            g.drawLine(x + 3, y + 15, x + 7, y + 14);
            g.drawLine(x + 13, y + 5, x + 15, y + 7);
            g.dispose();
        }
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static boolean isStartRequirementNotice(String message) {
        return message != null && message.contains("시작")
                && (message.contains("준비") || message.contains("참가자"));
    }

    private void clearRoomChat(int roomId) {
        if (currentRoomId == roomId) return;
        currentRoomId = roomId;
        if (chatArea != null) chatArea.setText("");
    }

    /** 방을 나가거나 방이 종료됐을 때 다음 대기실에 이전 채팅이 남지 않게 초기화합니다. */
    public void clearRoomSession() {
        clearRoomChat(-1);
    }

    private static boolean isValidRoomTitle(String title) {
        if (title == null || title.isEmpty() || title.length() > 30
                || title.indexOf('|') >= 0 || title.indexOf(',') >= 0) return false;
        for (int i = 0; i < title.length(); i++)
            if (Character.isISOControl(title.charAt(i))) return false;
        return true;
    }

    /** 시작 조건 안내를 화면 중앙에 잠깐 표시합니다. */
    private void showCenterNotice(String message) {
        centerNotice = message;
        centerNoticeUntil = System.currentTimeMillis() + 2200;
        if (!noticeTimer.isRunning()) noticeTimer.start();
        repaint();
    }

    private static Color slotBackground(String skin, SlotType type) {
        if (type == SlotType.CLOSED) return new Color(53, 37, 45);
        if (type != SlotType.HUMAN) return new Color(37, 40, 50);
        if ("GOLD".equalsIgnoreCase(skin)) return new Color(92, 70, 34);
        if ("SILVER".equalsIgnoreCase(skin)) return new Color(65, 74, 88);
        if ("PLATINUM".equalsIgnoreCase(skin)) return new Color(49, 71, 91);
        return new Color(54, 58, 69);
    }

    private static Color slotAccent(String skin, SlotType type) {
        if (type == SlotType.CLOSED) return new Color(184, 83, 100);
        if (type != SlotType.HUMAN) return new Color(75, 82, 98);
        if ("GOLD".equalsIgnoreCase(skin)) return new Color(235, 190, 85);
        if ("SILVER".equalsIgnoreCase(skin)) return new Color(180, 195, 215);
        if ("PLATINUM".equalsIgnoreCase(skin)) return new Color(110, 190, 235);
        return new Color(205, 210, 220);
    }

    @Override
    protected void paintChildren(Graphics graphics) {
        super.paintChildren(graphics);
        if (centerNotice == null) return;
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        long remaining = Math.max(0, centerNoticeUntil - System.currentTimeMillis());
        float fade = Math.min(1f, remaining / 500f);
        int alpha = Math.max(0, Math.min(255, Math.round(225 * fade)));
        Font font = UIUtils.getFont(Font.BOLD, 24f);
        FontMetrics metrics = g.getFontMetrics(font);
        while (font.getSize2D() > 14f && metrics.stringWidth(centerNotice) + 64 > getWidth() - 24) {
            font = font.deriveFont(font.getSize2D() - 1f);
            metrics = g.getFontMetrics(font);
        }
        g.setFont(font);
        int width = metrics.stringWidth(centerNotice) + 64;
        int height = 66;
        int x = (getWidth() - width) / 2;
        int y = (getHeight() - height) / 2;
        g.setColor(new Color(10, 13, 20, alpha));
        g.fillRoundRect(x, y, width, height, 22, 22);
        g.setColor(new Color(255, 205, 105, alpha));
        g.drawRoundRect(x, y, width, height, 22, 22);
        g.setColor(new Color(255, 255, 255, alpha));
        g.drawString(centerNotice, x + (width - metrics.stringWidth(centerNotice)) / 2,
                y + (height - metrics.getHeight()) / 2 + metrics.getAscent());
        g.dispose();
    }
}
