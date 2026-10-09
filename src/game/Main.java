package game;

import game.ui.MainFrame;
import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MainFrame mainFrame = new MainFrame(); //UI를 담당하는 메인프레임을 불러옵니다
            mainFrame.setVisible(true); //메인프레임을 보이게 설정합니다
        });
    }
}