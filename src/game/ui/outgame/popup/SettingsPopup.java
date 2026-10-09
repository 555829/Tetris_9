package game.ui.outgame.popup;

import game.SettingsManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;

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

        JButton muteBtn = new JButton(defaultValue == 0 ? "🔇" : "🔊"); //이모지가 안나오는 버그 ,폰트문제인지 확인할필요있음
        muteBtn.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        muteBtn.setPreferredSize(new Dimension(50, 32));

        JSlider slider = new JSlider(0, 100, defaultValue);
        slider.setPreferredSize(new Dimension(280, 32));
        slider.setOpaque(false);

        JTextField percentField = new JTextField(String.valueOf(defaultValue), 4);
        percentField.setFont(UIUtils.getFont(Font.BOLD, 13f));
        percentField.setHorizontalAlignment(JTextField.CENTER);

        slider.addChangeListener(e -> {
            int val = slider.getValue();
            muteBtn.setText(val == 0 ? "🔇" : "🔊");
            percentField.setText(String.valueOf(val));
            onVolumeChange.accept(val);
        });

        controlPanel.add(muteBtn);
        controlPanel.add(slider);
        controlPanel.add(percentField);

        rowPanel.add(titleLabel);
        rowPanel.add(controlPanel);

        return rowPanel;
    }

    //==================================================================================================================
    //키 설정 탭
    //==================================================================================================================
    private JPanel createKeyConfigTabPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setBackground(new Color(35, 40, 50));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        panel.add(createPlayerKeyGroup("1P 조작키", new String[][]{
                {"좌 이동", settings.p1Left}, {"우 이동", settings.p1Right},
                {"소프트드롭", settings.p1Soft}, {"하드드롭", settings.p1Hard}, {"회전", settings.p1Rotation}
        }));
        panel.add(createPlayerKeyGroup("2P 조작키", new String[][]{
                {"좌 이동", settings.p2Left}, {"우 이동", settings.p2Right},
                {"소프트드롭", settings.p2Soft}, {"하드드롭", settings.p2Hard}, {"회전", settings.p2Rotation}
        }));

        return panel;
    }

    //==================================================================================================================
    //키 설정 탭 부품 ui 부분
    //==================================================================================================================
    private JPanel createPlayerKeyGroup(String title, String[][] defaultKeys) {
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        JLabel titleLabel = UIUtils.createLabel(title, 16f, Color.CYAN);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        group.add(titleLabel);
        group.add(Box.createVerticalStrut(10));

        for (String[] keyPair : defaultKeys) {
            JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 2));
            row.setOpaque(false);

            JLabel actionLabel = UIUtils.createLabel(keyPair[0], 13f, Color.WHITE);
            actionLabel.setPreferredSize(new Dimension(80, 25));
            actionLabel.setHorizontalAlignment(SwingConstants.RIGHT);

            JButton keyBtn = new JButton(keyPair[1]);
            keyBtn.setFont(UIUtils.getFont(Font.BOLD, 12f));
            keyBtn.setPreferredSize(new Dimension(100, 25));

            row.add(actionLabel);
            row.add(keyBtn);
            group.add(row);
        }
        return group;
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