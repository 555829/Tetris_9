package game.ui;

import game.ranking.RankingEntry;
import game.ranking.RankingRepository;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Shown after a game ends: final score, name entry when the score makes the top list,
 * and the current ranking board.
 */
public class GameOverPanel extends JPanel
{
    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);
    private static final Color DIM = new Color(150, 150, 160);
    private static final Color ACCENT = new Color(102, 204, 204);
    private static final Color HIGHLIGHT = new Color(218, 170, 0);
    private static final Color BUTTON = new Color(45, 45, 58);

    private final RankingRepository ranking;

    private final JLabel resultLabel = new JLabel();
    private final JPanel entryRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final JTextField nameField = new JTextField(10);
    private final JButton saveButton = new JButton("SAVE");
    private final JButton menuButton = new JButton("MENU");
    private final JPanel table = new JPanel(new GridLayout(0, 4, 8, 2));

    private int score;
    private int level;
    private int lines;

    public GameOverPanel(RankingRepository ranking, Runnable onMenu)
    {
        this.ranking = ranking;

        setBackground(BACKGROUND);
        setLayout(new GridBagLayout());

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));

        column.add(centered(label("GAME OVER", 32f, ACCENT)));
        column.add(Box.createVerticalStrut(8));
        resultLabel.setForeground(TEXT);
        resultLabel.setFont(resultLabel.getFont().deriveFont(Font.BOLD, 14f));
        column.add(centered(resultLabel));
        column.add(Box.createVerticalStrut(16));

        entryRow.setOpaque(false);
        entryRow.add(label("NEW RECORD! NAME", 13f, HIGHLIGHT));
        nameField.setFont(nameField.getFont().deriveFont(14f));
        entryRow.add(nameField);
        style(saveButton);
        entryRow.add(saveButton);
        column.add(centered(entryRow));
        column.add(Box.createVerticalStrut(16));

        column.add(centered(label("TOP " + RankingRepository.MAX_ENTRIES, 14f, TEXT)));
        column.add(Box.createVerticalStrut(6));
        table.setOpaque(false);
        column.add(centered(table));
        column.add(Box.createVerticalStrut(16));

        style(menuButton);
        column.add(centered(menuButton));

        add(column);

        // Enter in the name field saves too; the field's own listener also stops Enter
        // from reaching the window's default button.
        saveButton.addActionListener(e -> saveEntry());
        nameField.addActionListener(e -> saveEntry());
        menuButton.addActionListener(e -> onMenu.run());
    }

    /** Fills the screen for a just-finished game. Call before switching to this screen. */
    public void showResult(int score, int level, int lines)
    {
        this.score = score;
        this.level = level;
        this.lines = lines;

        resultLabel.setText("SCORE " + score + "   LEVEL " + level + "   LINES " + lines);

        boolean qualifies = ranking.qualifies(score);
        entryRow.setVisible(qualifies);
        nameField.setText("");
        refreshTable(-1);

        if (qualifies)
            SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    /** The button Enter should press: SAVE while a name is being entered, otherwise MENU. */
    public JButton getPrimaryButton()
    {
        return entryRow.isVisible() ? saveButton : menuButton;
    }

    private void saveEntry()
    {
        if (!entryRow.isVisible())
            return;

        int rank = ranking.add(nameField.getText(), score, level, lines);
        entryRow.setVisible(false);
        refreshTable(rank);

        getRootPane().setDefaultButton(menuButton);
        menuButton.requestFocusInWindow();
    }

    private void refreshTable(int highlightRank)
    {
        table.removeAll();
        addRow("#", "NAME", "SCORE", "LV", DIM);

        List<RankingEntry> top = ranking.getTop();
        for (int i = 0; i < RankingRepository.MAX_ENTRIES; i++)
        {
            Color color = i == highlightRank ? HIGHLIGHT : TEXT;
            if (i < top.size())
            {
                RankingEntry e = top.get(i);
                addRow(String.valueOf(i + 1), e.getName(), String.valueOf(e.getScore()),
                        String.valueOf(e.getLevel()), color);
            }
            else
            {
                addRow(String.valueOf(i + 1), "-", "-", "-", DIM);
            }
        }

        // BoxLayout would otherwise stretch the grid to the widest row and spread the columns apart.
        table.setMaximumSize(table.getPreferredSize());
        table.revalidate();
        table.repaint();
    }

    private void addRow(String rank, String name, String score, String level, Color color)
    {
        table.add(label(rank, 13f, color));
        table.add(label(name, 13f, color));
        table.add(label(score, 13f, color));
        table.add(label(level, 13f, color));
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

    private static void style(JButton button)
    {
        button.setFocusPainted(false);
        button.setBackground(BUTTON);
        button.setForeground(TEXT);
        button.setFont(button.getFont().deriveFont(Font.BOLD, 14f));
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }
}