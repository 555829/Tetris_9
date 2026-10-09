package game.ui.common;

import javax.swing.*;
import java.awt.*;
import java.io.InputStream;

public class UIUtils {
    private static final String FONT_PATH = "/game/asset/GyeonggiTitle.ttf";
    public static Font customFont;

    static {
        try (InputStream is = UIUtils.class.getResourceAsStream(FONT_PATH)) {
            if (is != null) {
                Font loadedFont = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(loadedFont);
                customFont = loadedFont;
            } else {
                customFont = new Font("맑은 고딕", Font.BOLD, 14);
            }
        } catch (Exception e) {
            customFont = new Font("맑은 고딕", Font.BOLD, 14);
        }
    }

    public static Font getFont(int style, float size) {
        if (customFont == null) {
            customFont = new Font("맑은 고딕", Font.BOLD, 14);
        }
        return customFont.deriveFont(style, size);
    }

    public static JLabel createLabel(String text, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(getFont(Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    public static JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        b.setFont(getFont(Font.BOLD, 16f));
        b.setPreferredSize(new Dimension(220, 50));
        b.setMaximumSize(new Dimension(220, 50));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        b.setFocusPainted(false);
        return b;
    }

    public static JButton createHeaderButton(String text) {
        JButton b = new JButton(text);
        b.setFont(getFont(Font.BOLD, 14f));
        b.setForeground(Color.WHITE);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        return b;
    }

    public static JPanel createCenteredPanel(Color bg) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBackground(bg);
        return p;
    }

    // 🌟 [누락되었던 메서드 추가] 팝업창 기본 상자 UI 생성
    public static JPanel createBasePopupBox(int w, int h) {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(new Color(45, 45, 60));
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 100, 150), 2),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        p.setPreferredSize(new Dimension(w, h));
        return p;
    }
}