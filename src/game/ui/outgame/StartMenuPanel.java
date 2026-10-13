package game.ui.outgame;

import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;

//======================================================================================================================
// 게임 키면 나오는화면
//======================================================================================================================
public class StartMenuPanel extends JPanel {
    private MainFrame mainFrame;

    public StartMenuPanel(MainFrame mainFrame) {
        //ui 관련 기초설정 하는부분들
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setBackground(UIUtils.COLOR_BACKGROUND);
        JPanel group = UIUtils.createMenuSurface();
        group.add(Box.createVerticalStrut(12));

        //사용할 버튼들 모아두는 곳
        JButton playBtn = UIUtils.createStyledButton("플레이");
        JButton settingsBtn = UIUtils.createSecondaryButton("설정");
        JButton exitBtn = UIUtils.createSecondaryButton("게임 종료");

        //버튼이 어떻게 작동할지 결정하는 부분
        playBtn.addActionListener(e -> mainFrame.showScreen(MainFrame.TAG_SUB_MENU));
        settingsBtn.addActionListener(e -> mainFrame.showSettingsPopup());
        exitBtn.addActionListener(e -> mainFrame.showExitPopup());

        //버튼들을 어떻게 띄워줄지 설정하는 부분
        group.add(playBtn);
        group.add(Box.createVerticalStrut(12));
        group.add(settingsBtn);
        group.add(Box.createVerticalStrut(12));
        group.add(exitBtn);
        add(group);
    }
}
