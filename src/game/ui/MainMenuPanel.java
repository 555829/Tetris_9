package game.ui;


import game.ui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

// 게임 설정 화면: 최고 점수를 보여주고 모드와 시작 레벨을 고르게 함
// 게임을 어떻게 만드는지는 모르고 StartListener로 선택 결과만 전달하므로
// 나중에 GameManager가 자기 시작 로직을 그대로 연결할 수 있음
public class MainMenuPanel extends JPanel
{
    public interface StartListener
    {
        void start(GameMode mode, int level);
    }

    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);
    private static final Color ACCENT = new Color(102, 204, 204);
    private static final Color BUTTON = new Color(45, 45, 58);

    private final JLabel bestScoreLabel = new JLabel();
    private final Map<GameMode, JToggleButton> modeButtons = new EnumMap<>(GameMode.class);
    private final JToggleButton[] levelButtons;
    private final JButton startButton = new JButton("시작");
    private final JButton backButton = new JButton("뒤로");

    public MainMenuPanel(int maxLevel, StartListener onStart, Runnable onBack)
    {
        setBackground(BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        column.add(centered(label("테트리스", 44f, ACCENT)));
        column.add(centered(label("9조", 14f, TEXT)));
        column.add(Box.createVerticalStrut(36));

        bestScoreLabel.setForeground(TEXT);
        bestScoreLabel.setFont(UIUtils.getFont(Font.BOLD, 16f));
        column.add(centered(bestScoreLabel));
        setBestScore(0);
        column.add(Box.createVerticalStrut(28));

        column.add(centered(label("모드 선택", 13f, TEXT)));
        column.add(Box.createVerticalStrut(8));

        JPanel modeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        modeRow.setOpaque(false);
        ButtonGroup modeGroup = new ButtonGroup();
        for (GameMode mode : GameMode.values())
        {
            JToggleButton button = new JToggleButton(mode.getLabel());
            style(button);
            button.setEnabled(mode.isAvailable());
            if (!mode.isAvailable())
                button.setToolTipText("준비 중");
            modeGroup.add(button);
            modeRow.add(button);
            modeButtons.put(mode, button);
        }
        modeButtons.get(GameMode.SINGLE).setSelected(true);
        column.add(centered(modeRow));
        column.add(Box.createVerticalStrut(20));

        column.add(centered(label("레벨 선택", 13f, TEXT)));
        column.add(Box.createVerticalStrut(8));

        JPanel levelRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        levelRow.setOpaque(false);
        ButtonGroup group = new ButtonGroup();
        levelButtons = new JToggleButton[maxLevel];
        for (int i = 0; i < maxLevel; i++)
        {
            JToggleButton button = new JToggleButton(String.valueOf(i + 1));
            style(button);
            group.add(button);
            levelRow.add(button);
            levelButtons[i] = button;
        }
        levelButtons[0].setSelected(true);
        column.add(centered(levelRow));
        column.add(Box.createVerticalStrut(28));

        style(startButton);
        startButton.setFont(UIUtils.getFont(Font.BOLD, 18f));
        startButton.addActionListener(e -> onStart.start(getSelectedMode(), getSelectedLevel()));

        style(backButton);
        backButton.addActionListener(e -> onBack.run());

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        buttonRow.setOpaque(false);
        buttonRow.add(backButton);
        buttonRow.add(startButton);
        column.add(centered(buttonRow));

        add(column);
    }

    // 게임이 끝날 때마다 다시 호출해서 항상 최신 기록을 보여줌
    public void setBestScore(int score)
    {
        bestScoreLabel.setText("최고 점수  " + score);
    }

    public GameMode getSelectedMode()
    {
        for (Map.Entry<GameMode, JToggleButton> entry : modeButtons.entrySet())
            if (entry.getValue().isSelected())
                return entry.getKey();
        return GameMode.SINGLE;
    }

    public int getSelectedLevel()
    {
        for (int i = 0; i < levelButtons.length; i++)
            if (levelButtons[i].isSelected())
                return i + 1;
        return 1;
    }

    // Enter로 시작 버튼을 누를 수 있게 해서 마우스 없이도 메뉴 사용 가능
    public JButton getStartButton()
    {
        return startButton;
    }

    private static JLabel label(String text, float size, Color color)
    {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(UIUtils.getFont(Font.BOLD, size));
        return label;
    }

    private static JComponent centered(JComponent component)
    {
        component.setAlignmentX(Component.CENTER_ALIGNMENT);
        return component;
    }

    private static void style(AbstractButton button)
    {
        button.setFocusPainted(false);
        button.setBackground(BUTTON);
        button.setForeground(TEXT);
        button.setFont(UIUtils.getFont(Font.BOLD, 14f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        // 기본 Look and Feel이 선택된 토글 버튼을 밝은 색으로 칠하기 때문에
        // 밝은 글자를 어두운 색으로 바꿔야 글자가 보임
        if (button instanceof JToggleButton)
            button.addItemListener(e -> button.setForeground(button.isSelected() ? BACKGROUND : TEXT));
    }
}