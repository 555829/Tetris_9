package game.ui;

import game.ranking.RankingRepository;
import game.tetris.Tetris;
import game.tetris.input.InputSourceType;
import game.tetris.input.InputTranslator;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;

import javax.swing.*;
import java.awt.CardLayout;

/**
 * Temporary launcher: main menu -> game -> game over / ranking -> main menu.
 * The real game loop belongs to GameManager; delete this once Main/GameManager are wired up.
 */
public final class GuiTest
{
    // TetrisFlow counts gravity and lock delay in frames tuned for 60 fps.
    private static final int FRAME_MS = 1000 / 60;
    private static final int GAME_OVER_HOLD_FRAMES = 120;
    private static final int MAX_LEVEL = 5;
    // Must match the Board size created in Tetris' constructor.
    private static final int BOARD_WIDTH = 10;
    private static final int BOARD_HEIGHT = 20;

    private static final String MENU = "menu";
    private static final String GAME = "game";
    private static final String OVER = "over";

    private final JFrame frame = new JFrame("Tetris_9 - GUI test");
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final RankingRepository ranking = RankingRepository.openDefault();
    private final MainMenuPanel menu;
    private final GameOverPanel gameOver;

    private Tetris game;
    private GamePanel gamePanel;
    private int gameOverFrames;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(GuiTest::new);
    }

    private GuiTest()
    {
        menu = new MainMenuPanel(MAX_LEVEL, this::startGame);
        gameOver = new GameOverPanel(ranking, this::showMenu);
        screens.add(menu, MENU);
        screens.add(gameOver, OVER);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(screens);

        // Size the window for the game screen so switching cards doesn't resize it.
        // Creating a throwaway Tetris for this would register an extra global key listener.
        screens.setPreferredSize(GamePanel.sizeFor(BOARD_WIDTH, BOARD_HEIGHT));
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);

        showMenu();
        frame.setVisible(true);

        new Timer(FRAME_MS, e -> tick()).start();
    }

    private void showMenu()
    {
        menu.setBestScore(ranking.getBestScore());
        cards.show(screens, MENU);
        frame.getRootPane().setDefaultButton(menu.getStartButton());
    }

    private void startGame(int level)
    {
        if (gamePanel != null)
            screens.remove(gamePanel);

        game = newGame(level);
        gamePanel = new GamePanel(game);
        gameOverFrames = 0;

        screens.add(gamePanel, GAME);
        cards.show(screens, GAME);

        // Otherwise Enter during play would press the hidden START button and restart the game.
        frame.getRootPane().setDefaultButton(null);

        // Space is hard drop; if the START button kept focus, Space would also "click" it.
        gamePanel.setFocusable(true);
        gamePanel.requestFocusInWindow();
    }

    private void tick()
    {
        if (game == null)
            return;

        game.tick();
        gamePanel.repaint();

        if (!game.flow.isGameOver())
            return;

        // Keep the GAME OVER overlay visible for a moment before showing the ranking screen.
        if (++gameOverFrames < GAME_OVER_HOLD_FRAMES)
            return;

        gameOver.showResult(game.flow.getScore(), game.flow.getLevel(), game.flow.getLines());
        game = null;
        cards.show(screens, OVER);
        frame.getRootPane().setDefaultButton(gameOver.getPrimaryButton());
    }

    private static Tetris newGame(int level)
    {
        return new Tetris(
                InputSourceType.Keyboard1,
                new TetrominoQueue(new PieceGenerator_7Bag()),
                level);
    }
}