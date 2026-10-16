package game.ui;

import game.SettingsManager;
import game.tetris.Tetris;
import game.tetris.input.InputSourceType;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;

import game.ui.common.UIUtils;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// 플레이어마다 GamePanel을 하나씩 배치하고, 게임 중 프레임 루프를 돌리는 화면
// 1인용/AI 대전/2인용은 Tetris 객체 개수만 다르기 때문에 (GameMode 참고)
// 모드별 레이아웃 코드가 따로 없음
public class GameScreen extends JPanel
{
    // TetrisFlow의 중력, 고정 지연이 60fps 기준 프레임 단위로 계산되어 있음
    private static final int FRAME_MS = 1000 / 60;
    // 결과 화면으로 넘어가기 전에 게임 오버 표시를 잠깐 보여주는 시간
    private static final int GAME_OVER_HOLD_FRAMES = 120;
    private static final int PANEL_GAP = 24;

    private static final Color BACKGROUND = new Color(24, 24, 32);
    private static final Color TEXT = new Color(230, 230, 235);

    private final Consumer<GameResult> onFinished;
    private final SettingsManager settings;
    private final Timer timer = new Timer(FRAME_MS, e -> tick());
    private final List<Tetris> games = new ArrayList<>();

    private GameMode mode;
    private int level;
    private boolean finished;

    // ESC 메뉴가 열리고 닫힐 때 MainFrame에 알려서 메뉴 팝업을 보이거나 숨기게 함
    private Consumer<Boolean> menuListener = open -> {};
    private boolean menuShown;
    private int winner;
    private int finishedFrames;

    public GameScreen(Consumer<GameResult> onFinished)
    {
        this(onFinished, null);
    }

    public GameScreen(Consumer<GameResult> onFinished, SettingsManager settings)
    {
        this.onFinished = onFinished;
        this.settings = settings;
        setBackground(BACKGROUND);
        setLayout(new GridBagLayout());
        // 게임 중 포커스를 가져와서, 메뉴 버튼에 남은 포커스 때문에 Space/Enter가 버튼을 누르지 않게 함
        setFocusable(true);
    }

    public void start(GameMode mode, int level)
    {
        stop();

        this.mode = mode;
        this.level = level;
        finished = false;
        winner = -1;
        finishedFrames = 0;

        InputSourceType[] players = mode.getPlayers();
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(0, PANEL_GAP / 2, 0, PANEL_GAP / 2);

        for (int i = 0; i < players.length; i++)
        {
            Tetris game = new Tetris(players[i], new TetrominoQueue(new PieceGenerator_7Bag()), level, settings);
            games.add(game);

            gbc.gridx = i;
            add(playerColumn(playerName(players[i], i), new GamePanel(game), mode.isVersus()), gbc);
        }

        revalidate();
        repaint();
        requestFocusInWindow();
        timer.start();
    }

    // 루프를 멈추고 현재 게임들을 정리. 여러 번 호출해도 안전함
    public void stop()
    {
        timer.stop();
        if (menuShown)
        {
            menuShown = false;
            menuListener.accept(false);
        }
        for (Tetris game : games)
            game.close();
        games.clear();
        removeAll();
    }

    private void tick()
    {
        if (finished)
        {
            if (++finishedFrames >= GAME_OVER_HOLD_FRAMES)
                report();
            return;
        }

        // 한 명이 일시정지(또는 메뉴)하면 다른 보드도 멈춰야 함
        // 안 그러면 사람이 멈춘 동안 AI는 계속 플레이하게 됨
        // 일시정지한 게임은 계속 tick 해야 함. 일시정지 해제 키를 거기서 읽기 때문
        boolean someonePaused = false;
        for (Tetris game : games)
            if (game.flow.isPaused() || game.flow.isMenuOpen())
                someonePaused = true;

        for (Tetris game : games)
        {
            boolean paused = game.flow.isPaused() || game.flow.isMenuOpen();
            if (!someonePaused || paused)
                game.tick();
        }

        finished = checkFinished();
        updateMenu();
        repaint();
    }

    public void setMenuListener(Consumer<Boolean> listener)
    {
        menuListener = listener;
    }

    // 메뉴 상태가 바뀐 순간에만 알림 (매 프레임 팝업을 다시 띄우지 않도록)
    private void updateMenu()
    {
        boolean open = false;
        for (Tetris game : games)
            if (game.flow.isMenuOpen())
                open = true;

        if (open != menuShown)
        {
            menuShown = open;
            menuListener.accept(open);
        }
    }

    // 메뉴의 "계속하기": 메뉴를 연 게임의 메뉴를 닫음 (openMenu()는 열기/닫기를 번갈아 함)
    public void closeMenu()
    {
        for (Tetris game : games)
            if (game.flow.isMenuOpen())
                game.flow.openMenu();
        updateMenu();
        requestFocusInWindow();
    }

    // 메뉴의 "다시 시작": 같은 모드, 같은 시작 레벨로 새 게임
    public void restart()
    {
        start(mode, level);
        updateMenu();
    }


    private boolean checkFinished()
    {
        if (!mode.isVersus())
            return games.get(0).flow.isGameOver();

        boolean anyOver = false;
        int survivor = -1;
        for (int i = 0; i < games.size(); i++)
        {
            if (games.get(i).flow.isGameOver())
                anyOver = true;
            else if (survivor == -1)
                survivor = i;
        }

        // 대전은 한 보드라도 꽉 차면 끝나고, 아직 살아있는 보드가 승리
        if (anyOver)
            winner = survivor;
        return anyOver;
    }

    private void report()
    {
        Tetris player1 = games.get(0);
        GameResult result = new GameResult(mode, winner,
                player1.flow.getScore(), player1.flow.getLevel(), player1.flow.getLines());

        stop();
        onFinished.accept(result);
    }

    private static String playerName(InputSourceType type, int index)
    {
        return type == InputSourceType.AI ? "AI" : (index + 1) + "P";
    }

    private static JComponent playerColumn(String name, GamePanel panel, boolean showName)
    {
        JPanel column = new JPanel(new BorderLayout(0, 6));
        column.setOpaque(false);
        if (showName)
        {
            JLabel label = new JLabel(name, SwingConstants.CENTER);
            label.setForeground(TEXT);
            label.setFont(UIUtils.getFont(Font.BOLD, 16f));
            column.add(label, BorderLayout.NORTH);
        }
        column.add(panel, BorderLayout.CENTER);
        return column;
    }
}