package game.ui.outgame.popup;

import game.SettingsManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;

//======================================================================================================================
// 설정 팝업을 띄워줌
//======================================================================================================================
public class SettingsPopup extends JPanel {
    private MainFrame mainFrame;
    private SettingsManager settings;
    private JPanel popupBox;

    //==================================================================================================================
    //세팅팝업 전체적으로 관리하는 부분
    //==================================================================================================================
    public SettingsPopup(MainFrame mainFrame, SettingsManager settings) {
        //필요한 설정 모아둠과 동시에 gridbagcontains가 popupbox를 화면중앙으로 잡아줌
        this.mainFrame = mainFrame;
        this.settings = settings;
        setLayout(new GridBagLayout());
        setOpaque(false);
        popupBox = UIUtils.createBasePopupBox(680, 520);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(popupBox, gbc);

        //버튼 레이아웃
        JLabel titleLabel = UIUtils.createLabel("설정", 22f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(UIUtils.getFont(Font.BOLD, 14f));
        tabbedPane.addTab("사운드", createSoundTabPanel());
        tabbedPane.addTab("키 설정", createKeyConfigTabPanel());
        tabbedPane.addTab("화면", createDisplayTabPanel());
        JButton closeBtn = UIUtils.createStyledButton("저장 및 닫기");

        //버튼 역할 정해주는곳
        closeBtn.addActionListener(e -> {settings.save();mainFrame.hideSettingsPopup();});

        //버튼 위치 잡아주는곳
        popupBox.add(titleLabel);
        popupBox.add(Box.createVerticalStrut(15));
        popupBox.add(tabbedPane);
        popupBox.add(Box.createVerticalStrut(15));
        popupBox.add(closeBtn);
    }

    //==================================================================================================================
    //소리 탭 화면
    //==================================================================================================================
    private JPanel createSoundTabPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        panel.add(createVolumeRow("마스터 볼륨", settings.masterVolume, false, val -> settings.masterVolume = val));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createVolumeRow("BGM 볼륨", settings.bgmVolume, true, val -> settings.bgmVolume = val));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createVolumeRow("FX 볼륨", settings.fxVolume, true, val -> settings.fxVolume = val));

        return panel;
    }
    //==================================================================================================================
    //소리 탭 내부 부품 ui
    //==================================================================================================================
    private JPanel createVolumeRow(String title, int defaultValue, boolean isIndented, java.util.function.Consumer<Integer> onVolumeChange) {
        JPanel rowPanel = new JPanel();
        rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.Y_AXIS));
        rowPanel.setOpaque(false);
        if (isIndented) rowPanel.setBorder(BorderFactory.createEmptyBorder(0, 35, 0, 0));

        JLabel titleLabel = UIUtils.createLabel(title, 15f, Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel controlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        controlPanel.setOpaque(false);
        controlPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JButton muteBtn = UIUtils.createSecondaryButton("");
        muteBtn.setIcon(new SpeakerIcon(defaultValue == 0));
        muteBtn.setToolTipText(defaultValue == 0 ? "음소거 해제" : "음소거");
        muteBtn.setPreferredSize(new Dimension(50, 32));
        muteBtn.setMaximumSize(new Dimension(50, 32));

        JSlider slider = new JSlider(0, 100, defaultValue);
        slider.setPreferredSize(new Dimension(280, 32));
        slider.setOpaque(false);

        JTextField percentField = new JTextField(String.valueOf(defaultValue), 4);
        percentField.setFont(UIUtils.getFont(Font.BOLD, 13f));
        percentField.setHorizontalAlignment(JTextField.CENTER);
        percentField.setEditable(false);
        final int[] previousVolume = {defaultValue > 0 ? defaultValue : 50};

        muteBtn.addActionListener(e -> {
            if (slider.getValue() == 0) {
                slider.setValue(previousVolume[0]);
            } else {
                previousVolume[0] = slider.getValue();
                slider.setValue(0);
            }
        });

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            muteBtn.setIcon(new SpeakerIcon(val == 0));
            muteBtn.setToolTipText(val == 0 ? "음소거 해제" : "음소거");
            percentField.setText(String.valueOf(val));
            onVolumeChange.accept(val);
            if (val > 0) previousVolume[0] = val;
        });

        controlPanel.add(muteBtn);
        controlPanel.add(slider);
        controlPanel.add(percentField);

        rowPanel.add(titleLabel);
        rowPanel.add(controlPanel);

        return rowPanel;
    }

    //스피커 아이콘이 안나와서 ai로 처리했더니 그냥 그려버린 코드...
    //이미지 찾기 귀찮아서 그냥 사용함
    private static final class SpeakerIcon implements Icon {
        private final boolean muted;

        private SpeakerIcon(boolean muted) { this.muted = muted; }
        @Override public int getIconWidth() { return 22; }
        @Override public int getIconHeight() { return 22; }

        @Override
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            Graphics2D g2 = (Graphics2D) graphics.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(muted ? UIUtils.COLOR_MUTED : UIUtils.COLOR_ACCENT);
                int centerY = y + getIconHeight() / 2;
                Polygon speaker = new Polygon(
                        new int[]{x + 2, x + 7, x + 13, x + 13, x + 7, x + 2},
                        new int[]{centerY - 4, centerY - 4, centerY - 9, centerY + 9, centerY + 4, centerY + 4}, 6);
                g2.fill(speaker);
                if (muted) {
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(x + 16, y + 6, x + 21, y + 16);
                    g2.drawLine(x + 21, y + 6, x + 16, y + 16);
                } else {
                    Path2D wave = new Path2D.Float();
                    wave.moveTo(x + 16, y + 6);
                    wave.quadTo(x + 21, centerY, x + 16, y + 16);
                    g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.draw(wave);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    //==================================================================================================================
    //키 설정 탭
    //==================================================================================================================

    // 키 설정 구조를 다루기 위한 내부 클래스
    private static class KeyBindingItem {
        String label;
        String currentKey;
        java.util.function.Consumer<String> keySetter;

        public KeyBindingItem(String label, String currentKey, java.util.function.Consumer<String> keySetter) {
            this.label = label;
            this.currentKey = currentKey;
            this.keySetter = keySetter;
        }
    }

    private JPanel createKeyConfigTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(35, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // 1P 키 설정
        panel.add(createPlayerKeyGroup("1P 조작키", new KeyBindingItem[]{
                new KeyBindingItem("좌 이동", settings.p1Left, val -> settings.p1Left = val),
                new KeyBindingItem("우 이동", settings.p1Right, val -> settings.p1Right = val),
                new KeyBindingItem("소프트드롭", settings.p1Soft, val -> settings.p1Soft = val),
                new KeyBindingItem("하드드롭", settings.p1Hard, val -> settings.p1Hard = val),
                new KeyBindingItem("회전", settings.p1Rotation, val -> settings.p1Rotation = val),
                new KeyBindingItem("홀드", settings.p1Hold, val -> settings.p1Hold = val)
        }));

        // 2P 키 설정
        panel.add(createPlayerKeyGroup("2P 조작키", new KeyBindingItem[]{
                new KeyBindingItem("좌 이동", settings.p2Left, val -> settings.p2Left = val),
                new KeyBindingItem("우 이동", settings.p2Right, val -> settings.p2Right = val),
                new KeyBindingItem("소프트드롭", settings.p2Soft, val -> settings.p2Soft = val),
                new KeyBindingItem("하드드롭", settings.p2Hard, val -> settings.p2Hard = val),
                new KeyBindingItem("회전", settings.p2Rotation, val -> settings.p2Rotation = val),
                new KeyBindingItem("홀드", settings.p2Hold, val -> settings.p2Hold = val)
        }));

        return panel;
    }

    //==================================================================================================================
    //키 설정 탭 부품 (ui쪽)
    //==================================================================================================================
    private JPanel createPlayerKeyGroup(String title, KeyBindingItem[] items) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel titleLabel = UIUtils.createLabel(title, 16f, Color.CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.add(titleLabel);
        group.add(Box.createVerticalStrut(10));

        for (KeyBindingItem item : items) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
            row.setOpaque(false);

            JLabel actionLabel = UIUtils.createLabel(item.label, 13f, Color.WHITE);
            actionLabel.setPreferredSize(new Dimension(80, 25));
            actionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            //버튼 역할 설정
            JButton keyBtn = UIUtils.createSecondaryButton(item.currentKey);
            keyBtn.setFont(UIUtils.getBodyFont(Font.BOLD, 12f));
            keyBtn.setPreferredSize(new Dimension(100, 25));
            keyBtn.setMaximumSize(new Dimension(100, 32));
            keyBtn.setFocusable(true);

            //버튼 클릭 시 키 입력 대기
            keyBtn.addActionListener(e -> {
                keyBtn.setText("키 입력...");
                keyBtn.setBackground(new Color(255, 200, 80));
                keyBtn.setForeground(Color.BLACK);

                java.awt.event.KeyAdapter keyAdapter = new java.awt.event.KeyAdapter() {
                    @Override
                    public void keyPressed(java.awt.event.KeyEvent ke) {

                        ke.consume();
                        int code = ke.getKeyCode();

                        // ESC 누르면 취소
                        if (code == java.awt.event.KeyEvent.VK_ESCAPE) {
                            keyBtn.setText(item.currentKey);
                        } else {
                            String keyName = convertKeyCodeToString(code);
                            boolean alreadyUsed = false;
                            for (KeyBindingItem other : items) {
                                if (other != item && keyName.equalsIgnoreCase(other.currentKey)) {
                                    alreadyUsed = true;
                                    break;
                                }
                            }
                            if (alreadyUsed) {
                                JOptionPane.showMessageDialog(SettingsPopup.this,
                                        "같은 플레이어의 다른 동작에서 사용 중인 키입니다.",
                                        "키 설정", JOptionPane.WARNING_MESSAGE);
                                keyBtn.setText(item.currentKey);
                            } else {
                                item.currentKey = keyName;
                                item.keySetter.accept(keyName); // settings 객체 변수 업데이트
                                keyBtn.setText(keyName);
                            }
                        }

                        // 스타일 원복 및 리스너 제거
                        keyBtn.setBackground(UIUtils.COLOR_SURFACE_RAISED);
                        keyBtn.setForeground(UIUtils.COLOR_TEXT);
                        keyBtn.removeKeyListener(this);
                    }
                };

                keyBtn.addKeyListener(keyAdapter);
                keyBtn.requestFocusInWindow();
            });

            row.add(actionLabel);
            row.add(keyBtn);
            group.add(row);
        }
        return group;
    }

    // KeyCode를 문자열("LEFT", "SPACE", "A" 등)로 정규화(Normalization)하는
    // Helper 메서드는 입력 장치(키보드)나 환경마다 다르게 들어오는 키 입력 값을 하나의 일관된 문자열 형식으로 통일해 주는 보조 함수
    //-google 검색
    private String convertKeyCodeToString(int keyCode) {
        return switch (keyCode) {
            case java.awt.event.KeyEvent.VK_LEFT -> "LEFT";
            case java.awt.event.KeyEvent.VK_RIGHT -> "RIGHT";
            case java.awt.event.KeyEvent.VK_UP -> "UP";
            case java.awt.event.KeyEvent.VK_DOWN -> "DOWN";
            case java.awt.event.KeyEvent.VK_SPACE -> "SPACE";
            default -> java.awt.event.KeyEvent.getKeyText(keyCode).toUpperCase();
        };
    }
    //==================================================================================================================
    //디스플레이 탭 띄우는 부분
    //==================================================================================================================
    private JPanel createDisplayTabPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(new Color(35, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(40, 50, 40, 50));

        //창 모드 설정하는 부분
        JComboBox<String> modeCombo = new JComboBox<>(new String[]{"창 모드", "전체 화면", "테두리 없는 창"});
        modeCombo.setSelectedItem(settings.displayMode);

        //해상도 설정하는 부분
        JComboBox<String> resCombo = new JComboBox<>(new String[]{"1280 x 720", "1600 x 900", "1920 x 1080"});
        resCombo.setSelectedItem(settings.resolution);

        //버튼 역할 정해주는 부분
        JButton applyBtn = UIUtils.createStyledButton("화면 설정 적용");
        applyBtn.addActionListener(e -> settings.applyDisplaySettings(mainFrame, (String) modeCombo.getSelectedItem(), (String) resCombo.getSelectedItem()));

        //버튼 레이아웃 정하는 부분
        panel.add(modeCombo);
        panel.add(Box.createVerticalStrut(10));
        panel.add(resCombo);
        panel.add(Box.createVerticalStrut(20));
        panel.add(applyBtn);
        return panel;
    }

    @Override //팝업 띄웠을때 뒤에 반투명 배경효과 주는코드
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
