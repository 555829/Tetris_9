package game.ui;

import javax.swing.*;
import java.awt.*;
import java.util.function.IntConsumer;

/**
 * Title screen: shows the best score and lets the player pick a starting level.
 * It knows nothing about how a game is created; it only reports the chosen level
 * through the onStart callback, so GameManager can plug in its own start logic later.
 */
public class MainMenuPanel extends JPanel
{
    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);
    private static final Color ACCENT = new Color(102, 204, 204);
    private static final Color BUTTON = new Color(45, 45, 58);

    private final JLabel bestScoreLabel = new JLabel();
    private final JToggleButton[] levelButtons;
    private final JButton startButton = new JButton("START");

    public MainMenuPanel(int maxLevel, IntConsumer onStart)
    {
        setBackground(BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        column.add(centered(label("TETRIS", 44f, ACCENT)));
        column.add(centered(label("Team 9", 14f, TEXT)));
        column.add(Box.createVerticalStrut(36));

        bestScoreLabel.setForeground(TEXT);
        bestScoreLabel.setFont(bestScoreLabel.getFont().deriveFont(Font.BOLD, 16f));
        column.add(centered(bestScoreLabel));
        setBestScore(0);
        column.add(Box.createVerticalStrut(28));

        column.add(centered(label("SELECT LEVEL", 13f, TEXT)));
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
        startButton.setFont(startButton.getFont().deriveFont(Font.BOLD, 18f));
        startButton.addActionListener(e -> onStart.accept(getSelectedLevel()));
        column.add(centered(startButton));

        add(column);
    }

    /** Called again after each game so the title screen always shows the current record. */
    public void setBestScore(int score)
    {
        bestScoreLabel.setText("BEST SCORE  " + score);
    }

    public int getSelectedLevel()
    {
        for (int i = 0; i < levelButtons.length; i++)
            if (levelButtons[i].isSelected())
                return i + 1;
        return 1;
    }

    /** Lets Enter press START, so the menu is usable without the mouse. */
    public JButton getStartButton()
    {
        return startButton;
    }

    private static JLabel label(String text, float size, Color color)
    {
        JLabel label = new JLabel(text);
        label.setForeground(color);
        label.setFont(label.getFont().deriveFont(Font.BOLD, size));
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
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        // The default look-and-feel paints its own light fill on the selected toggle,
        // so the light label text must switch to dark to stay readable.
        if (button instanceof JToggleButton)
            button.addItemListener(e -> button.setForeground(button.isSelected() ? BACKGROUND : TEXT));
    }
}