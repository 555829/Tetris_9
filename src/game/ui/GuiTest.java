package game.ui;

import game.tetris.Tetris;
import game.tetris.input.InputSourceType;
import game.tetris.input.InputTranslator;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;

import javax.swing.*;
import java.awt.CardLayout;

/**
 * Temporary launcher: main menu -> game -> back to menu on game over.
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

    private final JFrame frame = new JFrame("Tetris_9 - GUI test");
    private final CardLayout cards = new CardLayout();
    private final JPanel screens = new JPanel(cards);
    private final MainMenuPanel menu;

    private Tetris game;
    private GamePanel gamePanel;
    private int bestScore;
    private int gameOverFrames;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(GuiTest::new);
    }

    private GuiTest()
    {
        menu = new MainMenuPanel(MAX_LEVEL, this::startGame);
        screens.add(menu, MENU);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(screens);
        frame.getRootPane().setDefaultButton(menu.getStartButton());

        // Size the window for the game screen so switching cards doesn't resize it.
        // Creating a throwaway Tetris for this would register an extra global key listener.
        screens.setPreferredSize(GamePanel.sizeFor(BOARD_WIDTH, BOARD_HEIGHT));
        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

        new Timer(FRAME_MS, e -> tick()).start();
    }

    private void startGame(int level)
    {
        // TetrisFlow has no starting-level option yet, so the chosen level is only logged for now.
        System.out.println("Start requested at level " + level);

        if (gamePanel != null)
            screens.remove(gamePanel);

        game = newGame(level);
        gamePanel = new GamePanel(game);
        gameOverFrames = 0;

        screens.add(gamePanel, GAME);
        cards.show(screens, GAME);

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

        // Keep the GAME OVER overlay visible for a moment before returning to the menu.
        if (++gameOverFrames < GAME_OVER_HOLD_FRAMES)
            return;

        bestScore = Math.max(bestScore, game.flow.getScore());
        menu.setBestScore(bestScore);
        game = null;
        cards.show(screens, MENU);
    }

    private static Tetris newGame(int level)
    {
        return new Tetris(
                new InputTranslator(InputSourceType.Keyboard1),
                new TetrominoQueue(new PieceGenerator_7Bag()),
                level);
    }
}