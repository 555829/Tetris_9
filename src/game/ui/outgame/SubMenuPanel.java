package game.ui.outgame;

import game.NetworkManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;

public class SubMenuPanel extends JPanel {
    private MainFrame mainFrame;

    public SubMenuPanel(MainFrame mainFrame) {
        //ui 관련 기초설정 하는부분들
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(25, 25, 35));
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        //사용할 버튼들 모아두는 곳
        JButton singleplayBtn = UIUtils.createStyledButton("오프라인 플레이");
        JButton multiplayBtn = UIUtils.createStyledButton("온라인 멀티 플레이");
        JButton backBtn = UIUtils.createStyledButton("메인 메뉴로");

        //버튼이 어떻게 작동할지 결정하는 부분
        singleplayBtn.addActionListener(e -> mainFrame.showGameSetup());
        multiplayBtn.addActionListener(e -> mainFrame.showAuthPopup());
        backBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.TAG_MENU));

        //버튼들을 어떻게 띄워줄지 설정하는 부분
        group.add(singleplayBtn);
        group.add(Box.createVerticalStrut(15)); //15만큼 버튼 띄워주기
        group.add(multiplayBtn);
        group.add(Box.createVerticalStrut(15));
        group.add(backBtn);
        add(group);
    }
}