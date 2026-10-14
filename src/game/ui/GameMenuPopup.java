package game.ui;

import game.ui.common.UIUtils;

import javax.swing.*;
import java.awt.*;

// 게임 중 ESC를 누르면 뜨는 메뉴 (계속하기 / 다시 시작 / 메인으로)
// MainFrame을 직접 알지 않고 버튼마다 할 일만 Runnable로 받음
// 이 팝업은 어떤 버튼이 눌렸는지만 알리고, 실제 화면 전환은 MainFrame이 결정
public class GameMenuPopup extends JPanel
{
    public GameMenuPopup(Runnable onResume, Runnable onRestart, Runnable onHome)
    {
        setLayout(new GridBagLayout());
        setOpaque(false);

        JPanel box = UIUtils.createBasePopupBox(320, 300);
        add(box, new GridBagConstraints());

        JLabel title = UIUtils.createLabel("일시정지 메뉴", 20f, Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton resumeBtn = UIUtils.createStyledButton("계속하기");
        JButton restartBtn = UIUtils.createSecondaryButton("다시 시작");
        JButton homeBtn = UIUtils.createSecondaryButton("메인으로");

        resumeBtn.addActionListener(e -> onResume.run());
        restartBtn.addActionListener(e -> onRestart.run());
        homeBtn.addActionListener(e -> onHome.run());

        Dimension size = new Dimension(200, 44);
        box.add(Box.createVerticalStrut(10));
        box.add(title);
        box.add(Box.createVerticalStrut(24));
        for (JButton btn : new JButton[]{resumeBtn, restartBtn, homeBtn})
        {
            btn.setPreferredSize(size);
            btn.setMaximumSize(size);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
            box.add(btn);
            box.add(Box.createVerticalStrut(12));
        }

        JLabel hint = UIUtils.createLabel("ESC: 계속하기", 12f, new Color(170, 170, 180));
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(hint);
    }

    @Override // 뒤의 게임 화면을 어둡게 가림
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 150));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}