package game.ui.outgame;

import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;

public class StartMenuPanel extends JPanel {
    private MainFrame mainFrame;

    public StartMenuPanel(MainFrame mainFrame) {
        //ui 관련 기초설정 하는부분들
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(new Color(25, 25, 35));
        JPanel group = new JPanel();
        group.setLayout(new BoxLayout(group, BoxLayout.Y_AXIS));
        group.setOpaque(false);

        //사용할 버튼들 모아두는 곳
        JButton playBtn = UIUtils.createStyledButton("플레이");
        JButton settingsBtn = UIUtils.createStyledButton("설정");
        JButton exitBtn = UIUtils.createStyledButton("게임 종료");

        //버튼이 어떻게 작동할지 결정하는 부분
        playBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.TAG_SUB_MENU));
        settingsBtn.addActionListener(e -> mainFrame.showSettingsPopup());
        exitBtn.addActionListener(e -> mainFrame.showExitPopup());

        //버튼들을 어떻게 띄워줄지 설정하는 부분
        group.add(playBtn);
        group.add(Box.createVerticalStrut(15)); //15만큼 버튼 띄워주기
        group.add(settingsBtn);
        group.add(Box.createVerticalStrut(15));
        group.add(exitBtn);
        add(group);
    }
}