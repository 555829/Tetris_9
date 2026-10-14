package game.ui;

import game.NetworkManager;
import game.SettingsManager;
import game.ranking.RankingRepository;
import game.ui.multigame.MultiGameScreen;
import game.ui.multigame.MultiMatchResultPanel;
import game.ui.outgame.*;
import game.ui.outgame.popup.*;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

//======================================================================================================================
// 메인프레임
//======================================================================================================================
public class MainFrame extends JFrame {

    private final CardLayout cardLayout = new CardLayout();
    private final JPanel mainPanel = new JPanel(cardLayout);
    private final JLayeredPane layeredPane;

    public static final String TAG_MENU = "MENU";
    public static final String TAG_SUB_MENU = "SUB_MENU";
    public static final String TAG_MULTI_LOBBY = "MULTI_LOBBY";
    public static final String TAG_MULTI_ROOM = "MULTI_ROOM";
    public static final String TAG_GAME_SETUP = "GAME_SETUP";
    public static final String TAG_GAME = "GAME";
    public static final String TAG_GAME_OVER = "GAME_OVER";
    public static final String TAG_MULTI_GAME = "MULTI_GAME";
    public static final String TAG_MULTI_RESULT = "MULTI_RESULT";

    private static final int MAX_LEVEL = 5;

    private final RankingRepository ranking = RankingRepository.openDefault();
    private final MainMenuPanel gameSetupPanel;
    private final GameScreen gameScreen;
    private final GameOverPanel gameOverPanel;

    // 패널 필드 선언
    private final MultiRoomPanel multiRoomPanel;
    private final MultiLobbyPanel multiLobbyPanel;
    private final MultiGameScreen multiGameScreen;
    private final MultiMatchResultPanel multiMatchResultPanel;

    private final SettingsPopup settingsPopup;
    private final ExitPopup exitPopup;
    private final GameMenuPopup gameMenuPopup;
    private final AuthPopup authPopup;
    private final LogoutPopup logoutPopup;
    private final ProfilePopup profilePopup;
    private final ShopPopup shopPopup;
    private final RoomLeavePopup roomLeavePopup;
    private final RoomCreatePopup roomCreatePopup;

    private final SettingsManager settings = new SettingsManager();
    private final Consumer<String> globalPacketListener = this::handleGlobalPacket;

    public MainFrame() {
        try {
            settings.load();
        } catch (Exception e) {
            System.err.println("설정로딩실패" + e.getMessage());
        }

        NetworkManager.getInstance().addPacketListener(globalPacketListener);

        // 각 화면은 한 번만 만들고 CardLayout에서 재사용합니다.
        multiRoomPanel = new MultiRoomPanel(this);
        multiLobbyPanel = new MultiLobbyPanel(this);
        multiMatchResultPanel = new MultiMatchResultPanel(
                () -> showScreen(TAG_MULTI_ROOM),
                () -> {
                    NetworkManager.getInstance().leaveRoom();
                    multiRoomPanel.clearRoomSession();
                    NetworkManager.getInstance().requestRoomList();
                    showScreen(TAG_MULTI_LOBBY);
                });
        multiGameScreen = new MultiGameScreen(reason -> {
            System.out.println("멀티게임 종료 사유: " + reason);
            if ("LEFT_GAME".equals(reason)) {
                multiRoomPanel.clearRoomSession();
                NetworkManager.getInstance().requestRoomList();
                showScreen(TAG_MULTI_LOBBY);
            } else if (reason != null && reason.startsWith("RES_MATCH_RESULT|")) {
                // 서버가 확정한 결과를 화면에 전달한 다음 결과 페이지로 이동합니다.
                multiMatchResultPanel.showResultPacket(reason);
                showScreen(TAG_MULTI_RESULT);
            } else {
                showScreen(TAG_MULTI_ROOM);
            }
        }, settings, this::showGameLeavePopup, this::showSettingsPopup);
        setTitle("테트리스");
        java.net.URL appIcon = getClass().getResource("/game/asset/tetrisLogo.jpg");
        if (appIcon != null) setIconImage(new ImageIcon(appIcon).getImage());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1280, 720);
        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);

        layeredPane = new JLayeredPane();
        layeredPane.setLayout(new LayoutManager() {
            @Override public void addLayoutComponent(String name, Component comp) {}
            @Override public void removeLayoutComponent(Component comp) {}
            @Override public Dimension preferredLayoutSize(Container parent) { return parent.getSize(); }
            @Override public Dimension minimumLayoutSize(Container parent) { return parent.getSize(); }
            @Override
            public void layoutContainer(Container parent) {
                int w = parent.getWidth(), h = parent.getHeight();
                for (Component c : parent.getComponents()) c.setBounds(0, 0, w, h);
            }
        });

        layeredPane.add(mainPanel, JLayeredPane.DEFAULT_LAYER);
        add(layeredPane);

        // 화면 전환에 사용할 각 패널을 CardLayout에 등록합니다.
        mainPanel.add(new StartMenuPanel(this), TAG_MENU);
        mainPanel.add(new SubMenuPanel(this), TAG_SUB_MENU);
        mainPanel.add(multiLobbyPanel, TAG_MULTI_LOBBY);
        mainPanel.add(multiRoomPanel, TAG_MULTI_ROOM);
        mainPanel.add(multiGameScreen, TAG_MULTI_GAME);
        mainPanel.add(multiMatchResultPanel, TAG_MULTI_RESULT);

        gameSetupPanel = new MainMenuPanel(MAX_LEVEL, this::startGame, () -> showScreen(TAG_SUB_MENU));
        gameScreen = new GameScreen(this::showGameOver, settings);
        gameOverPanel = new GameOverPanel(ranking, this::showGameSetup);
        mainPanel.add(gameSetupPanel, TAG_GAME_SETUP);
        mainPanel.add(gameScreen, TAG_GAME);
        mainPanel.add(gameOverPanel, TAG_GAME_OVER);

        // 팝업 생성
        settingsPopup = new SettingsPopup(this, settings);
        settingsPopup.setVisible(false);
        layeredPane.add(settingsPopup, JLayeredPane.POPUP_LAYER);

        exitPopup = new ExitPopup(this);
        exitPopup.setVisible(false);
        layeredPane.add(exitPopup, JLayeredPane.POPUP_LAYER);
        // 게임 중 ESC 메뉴
        gameMenuPopup = new GameMenuPopup(
                gameScreen::closeMenu,
                gameScreen::restart,
                () -> { gameScreen.stop(); showGameSetup(); });
        gameMenuPopup.setVisible(false);
        layeredPane.add(gameMenuPopup, JLayeredPane.POPUP_LAYER);
        gameScreen.setMenuListener(open -> {
            if (open) UIUtils.animatePopupIn(gameMenuPopup);
            else gameMenuPopup.setVisible(false);
            layeredPane.revalidate();
            layeredPane.repaint();
        });


        authPopup = new AuthPopup(this);
        authPopup.setVisible(false);
        layeredPane.add(authPopup, JLayeredPane.POPUP_LAYER);

        logoutPopup = new LogoutPopup(this);
        logoutPopup.setVisible(false);
        layeredPane.add(logoutPopup, JLayeredPane.POPUP_LAYER);

        profilePopup = new ProfilePopup(this);
        profilePopup.setVisible(false);
        layeredPane.add(profilePopup, JLayeredPane.POPUP_LAYER);

        shopPopup = new ShopPopup(this);
        shopPopup.setVisible(false);
        layeredPane.add(shopPopup, JLayeredPane.POPUP_LAYER);

        roomLeavePopup = new RoomLeavePopup(this::hideRoomLeavePopup);
        roomLeavePopup.setVisible(false);
        layeredPane.add(roomLeavePopup, JLayeredPane.POPUP_LAYER);

        roomCreatePopup = new RoomCreatePopup(this::hideRoomCreatePopup);
        roomCreatePopup.setVisible(false);
        layeredPane.add(roomCreatePopup, JLayeredPane.POPUP_LAYER);

        showScreen(TAG_MENU);
    }

    //메인 화면 전환, 프로필 갱신
    private void handleGlobalPacket(String packet) {
        if (packet == null || packet.trim().isEmpty()) return;
        String command = packet.split("\\|", 2)[0];

        if ("RES_USER_INFO".equals(command)) {
            profilePopup.refreshData();
        } else if ("RES_ROOM_CLOSED".equals(command)) {
            multiGameScreen.stop();
            multiRoomPanel.clearRoomSession();
            NetworkManager.getInstance().requestRoomList();
            showScreen(TAG_MULTI_LOBBY);
        }
    }

    // 참가한 사람 정보 전달
    public void startMultiGame(int mySlotIndex, boolean isHost, MultiRoomPanel.SlotType[] slots,
                               String[] nicknames, String modeLabel) {
        if (multiGameScreen != null) {
            multiGameScreen.startMultiGame(mySlotIndex, isHost, slots, nicknames, 1, modeLabel);
            showScreen(TAG_MULTI_GAME);
        }
    }

    //레이아웃 재개산 (ai가 쓴 코드)
    public void showScreen(String tag) {
        getRootPane().setDefaultButton(null);
        cardLayout.show(mainPanel, tag);
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    public void showGameSetup() {
        gameSetupPanel.setBestScore(ranking.getBestScore());
        showScreen(TAG_GAME_SETUP);
        getRootPane().setDefaultButton(gameSetupPanel.getStartButton());
    }

    private void startGame(GameMode mode, int level) {
        showScreen(TAG_GAME);
        gameScreen.start(mode, level);
    }

    private void showGameOver(GameResult result) {
        gameOverPanel.showResult(result);
        showScreen(TAG_GAME_OVER);
        getRootPane().setDefaultButton(gameOverPanel.getPrimaryButton());
    }

    public void showSettingsPopup() { UIUtils.animatePopupIn(settingsPopup); layeredPane.revalidate(); }
    public void hideSettingsPopup() { settingsPopup.setVisible(false); layeredPane.revalidate(); }
    public void showExitPopup() { UIUtils.animatePopupIn(exitPopup); layeredPane.revalidate(); }
    public void hideExitPopup() { exitPopup.setVisible(false); layeredPane.revalidate(); }
    public void showAuthPopup() { UIUtils.animatePopupIn(authPopup); layeredPane.revalidate(); }
    public void hideAuthPopup() { authPopup.setVisible(false); layeredPane.revalidate(); }
    public void showLogoutPopup() { UIUtils.animatePopupIn(logoutPopup); layeredPane.revalidate(); }
    public void hideLogoutPopup() { logoutPopup.setVisible(false); layeredPane.revalidate(); }

    public void showProfilePopup() {
        if (profilePopup != null) {
            if (NetworkManager.getInstance().isConnected) NetworkManager.getInstance().requestUserInfo();
            profilePopup.refreshData();
            UIUtils.animatePopupIn(profilePopup);
            layeredPane.moveToFront(profilePopup);
            layeredPane.revalidate();
        }
    }
    public void hideProfilePopup() { profilePopup.setVisible(false); layeredPane.revalidate(); }
    public void showShopPopup() {
        if (NetworkManager.getInstance().isConnected) NetworkManager.getInstance().requestUserInfo();
        UIUtils.animatePopupIn(shopPopup);
        layeredPane.revalidate();
    }
    public void hideShopPopup() { shopPopup.setVisible(false); layeredPane.revalidate(); }

    public void showRoomLeavePopup(Runnable leaveAction) {
        roomLeavePopup.showPopup(leaveAction);
        layeredPane.moveToFront(roomLeavePopup);
        layeredPane.revalidate();
    }

    public void hideRoomLeavePopup() {
        roomLeavePopup.hidePopup();
        layeredPane.revalidate();
    }

    public void showRoomCreatePopup(java.util.function.BiConsumer<String, String> createAction) {
        roomCreatePopup.showPopup(createAction);
        layeredPane.moveToFront(roomCreatePopup);
        layeredPane.revalidate();
    }

    public void hideRoomCreatePopup() {
        roomCreatePopup.hidePopup();
        layeredPane.revalidate();
    }

    private void showGameLeavePopup() {
        showRoomLeavePopup(multiGameScreen::confirmLeaveGame);
    }
}