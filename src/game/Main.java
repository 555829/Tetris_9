package game;

import javax.swing.SwingUtilities;

public class Main
{
    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(MainManager::new);
    }
}