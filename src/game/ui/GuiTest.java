package game.ui;

import game.tetris.Tetris;
import game.tetris.input.InputSourceType;
import game.tetris.input.InputTranslator;
import game.tetris.tetrominoQueue.PieceGenerator_7Bag;
import game.tetris.tetrominoQueue.TetrominoQueue;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

/**
 * Temporary launcher for testing GamePanel on its own.
 * The real game loop belongs to GameManager; delete this once Main/GameManager are wired up.
 */
public final class GuiTest
{
    // TetrisFlow counts gravity and lock delay in frames tuned for 60 fps.
    private static final int FRAME_MS = 1000 / 60;

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() ->
        {
            Tetris game = new Tetris(
                    new InputTranslator(InputSourceType.Keyboard1),
                    new TetrominoQueue(new PieceGenerator_7Bag()));

            GamePanel panel = new GamePanel(game);

            JFrame frame = new JFrame("Tetris_9 - GUI test");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(panel);
            frame.pack();
            frame.setResizable(false);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            new Timer(FRAME_MS, e ->
            {
                game.tick();
                panel.repaint();
            }).start();
        });
    }
}