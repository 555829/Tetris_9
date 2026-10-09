package game.ui;

import game.SettingsManager;
import game.ui.outgame.*;
import game.ui.outgame.popup.*;
import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    //ui에 필요한 레이아웃 모아놓은곳
    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private JLayeredPane layeredPane;

    //패널 선언하기
    public static final String TAG_MENU = "MENU";
    public static final String TAG_SUB_MENU = "SUB_MENU";

    //팝업 선언하기
    private SettingsPopup settingsPopup;
    private ExitPopup exitPopup;
    private AuthPopup authPopup;

    //세팅매니져 사용선언하기
    private SettingsManager settings = new SettingsManager();

    //==================================================================================================================
    //전체적 창 제어 (인게임은 제어하지 않습니다)
    //==================================================================================================================
    public MainFrame() {
        //설정값 못불러왔을때 하는 예외처리
        try {
            settings.load();
        } catch (Exception e) {
            System.err.println("설정로딩실패" + e.getMessage());
        }

        //테트리스 창 기본값 설정하는 부분
        setTitle("테트리스");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        //화면 크기 조정해도 내부 요소들이 그에 맞춰주는 코드
        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new LayoutManager() {
            @Override public void addLayoutComponent(String name, Component comp) {}
            @Override public void removeLayoutComponent(Component comp) {}
            @Override public Dimension preferredLayoutSize(Container parent) { return parent.getSize(); }
            @Override public Dimension minimumLayoutSize(Container parent) { return parent.getSize(); }
            @Override
            public void layoutContainer(Container parent) {
                int w = parent.getWidth();
                int h = parent.getHeight();
                for (Component c : parent.getComponents()) {
                    c.setBounds(0, 0, w, h);
                }
            }
        });

        //ui 층 구성하는 요소
        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        add(layeredPane);

        // 메인 카드 패널 추가
        mainPanel.add(new StartMenuPanel(this), TAG_MENU);
        mainPanel.add(new SubMenuPanel(this), TAG_SUB_MENU);

        // 팝업 생성 및 추가
        settingsPopup = new SettingsPopup(this, settings);
        settingsPopup.setVisible(false);
        layeredPane.add(settingsPopup, JLayeredPane.POPUP_LAYER);

        exitPopup = new ExitPopup(this);
        exitPopup.setVisible(false);
        layeredPane.add(exitPopup, JLayeredPane.POPUP_LAYER);

        authPopup = new AuthPopup(this);
        authPopup.setVisible(false);
        layeredPane.add(authPopup, JLayeredPane.POPUP_LAYER);

        // 시작 화면 출력
        showScreen(TAG_MENU);
    }

    //==================================================================================================================
    //팝업 레이아웃
    //==================================================================================================================
    public void showScreen(String tag) {
        cardLayout.show(mainPanel, tag);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showSettingsPopup() {
        settingsPopup.setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void hideSettingsPopup() {
        settingsPopup.setVisible(false);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void showExitPopup() {
        exitPopup.setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void hideExitPopup() {
        exitPopup.setVisible(false);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void showAuthPopup() {
        authPopup.setVisible(true);
        layeredPane.revalidate();
        layeredPane.repaint();
    }

    public void hideAuthPopup() {
        authPopup.setVisible(false);
        layeredPane.revalidate();
        layeredPane.repaint();
    }
}