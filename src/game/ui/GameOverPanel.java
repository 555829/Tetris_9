package game.ui;

import game.ranking.RankingEntry;
import game.ranking.RankingRepository;

import game.ui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;

// 게임이 끝난 뒤 보여주는 화면: 최종 점수, 랭킹에 들면 이름 입력, 현재 랭킹표
public class GameOverPanel extends JPanel
{
    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);
    private static final Color DIM = new Color(150, 150, 160);
    private static final Color ACCENT = new Color(102, 204, 204);
    private static final Color HIGHLIGHT = new Color(218, 170, 0);
    private static final Color BUTTON = new Color(45, 45, 58);

    private final RankingRepository ranking;

    private final JLabel headlineLabel = label("게임 오버", 32f, ACCENT);
    private final JLabel resultLabel = new JLabel();
    private final JPanel entryRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
    private final JTextField nameField = new JTextField(10);
    private final JButton saveButton = new JButton("저장");
    private final JButton menuButton = new JButton("메뉴");
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

        column.add(centered(headlineLabel));
        column.add(Box.createVerticalStrut(8));
        resultLabel.setForeground(TEXT);
        resultLabel.setFont(UIUtils.getFont(Font.BOLD, 14f));
        column.add(centered(resultLabel));
        column.add(Box.createVerticalStrut(16));

        entryRow.setOpaque(false);
        entryRow.add(label("신기록! 이름", 13f, HIGHLIGHT));
        nameField.setFont(nameField.getFont().deriveFont(14f));
        entryRow.add(nameField);
        style(saveButton);
        entryRow.add(saveButton);
        column.add(centered(entryRow));
        column.add(Box.createVerticalStrut(16));

        column.add(centered(label("랭킹 TOP " + RankingRepository.MAX_ENTRIES, 14f, TEXT)));
        column.add(Box.createVerticalStrut(6));
        table.setOpaque(false);
        column.add(centered(table));
        column.add(Box.createVerticalStrut(16));

        style(menuButton);
        column.add(centered(menuButton));

        add(column);

        // 이름 칸에서 Enter를 눌러도 저장됨
        // 텍스트 필드의 리스너가 Enter를 먼저 받아서 창의 기본 버튼까지 가지 않음
        saveButton.addActionListener(e -> saveEntry());
        nameField.addActionListener(e -> saveEntry());
        menuButton.addActionListener(e -> onMenu.run());
    }

    // 방금 끝난 게임 결과로 화면을 채움. 이 화면으로 전환하기 전에 호출
    public void showResult(GameResult result)
    {
        this.score = result.getScore();
        this.level = result.getLevel();
        this.lines = result.getLines();

        headlineLabel.setText(headline(result));
        resultLabel.setText("점수 " + score + "   레벨 " + level + "   줄 " + lines);

        // 랭킹에는 1인용 점수만 기록함
        // 대전은 상대가 지면 바로 끝나서 1인용 점수와 비교할 수 없기 때문
        boolean qualifies = !result.getMode().isVersus() && ranking.qualifies(score);
        entryRow.setVisible(qualifies);
        nameField.setText("");
        refreshTable(-1);

        if (qualifies)
            SwingUtilities.invokeLater(nameField::requestFocusInWindow);
    }

    private static String headline(GameResult result)
    {
        if (!result.getMode().isVersus())
            return "게임 오버";
        if (result.getWinner() < 0)
            return "무승부";
        if (result.getWinner() == 0)
            return "1P 승리!";
        return result.getMode() == GameMode.VS_AI ? "AI 승리" : (result.getWinner() + 1) + "P 승리!";
    }

    // Enter가 누를 버튼: 이름 입력 중이면 저장, 아니면 메뉴
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
        addRow("순위", "이름", "점수", "레벨", DIM);

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

        // 이걸 안 하면 BoxLayout이 표를 가로로 늘려서 열 간격이 벌어짐
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
        label.setFont(UIUtils.getFont(Font.BOLD, size));
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
        button.setFont(UIUtils.getFont(Font.BOLD, 14f));
        button.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
    }
}