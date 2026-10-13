package game.ui.common;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.InputStream;
//======================================================================================================================
//ui 재사용하는것들 모음, 버튼이나 글꼴 이런거 (이거도 ai 리팩토링함)
//======================================================================================================================
public class UIUtils {
    public static final Color COLOR_BACKGROUND = new Color(23, 26, 35);
    public static final Color COLOR_SURFACE = new Color(39, 44, 57);
    public static final Color COLOR_SURFACE_RAISED = new Color(49, 56, 72);
    public static final Color COLOR_BORDER = new Color(77, 88, 110);
    public static final Color COLOR_ACCENT = new Color(92, 211, 218);
    public static final Color COLOR_TEXT = new Color(238, 241, 248);
    public static final Color COLOR_MUTED = new Color(165, 174, 192);
    public static final Color COLOR_SUCCESS = new Color(112, 225, 164);
    public static final Color COLOR_WARNING = new Color(255, 205, 105);

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

    /** 본문과 긴 설명에 쓰는 시스템 한글 글꼴을 제공합니다. */
    public static Font getBodyFont(int style, float size) {
        return new Font("맑은 고딕", style, Math.max(1, Math.round(size)));
    }

    /** 시작 메뉴와 하위 메뉴에서 공통으로 쓰는 중앙 카드 컨테이너를 만듭니다. */
    public static JPanel createMenuSurface() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setBackground(COLOR_SURFACE);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER),
                BorderFactory.createEmptyBorder(30, 46, 30, 46)));
        return panel;
    }

    /** 게임 로고를 비율을 유지해 메뉴와 제목 영역에 배치합니다. */
    public static JLabel createLogoLabel(int width) {
        java.net.URL logoUrl = UIUtils.class.getResource("/game/asset/tetrisLogo.jpg");
        if (logoUrl == null) return createLabel("TETRIS", 28f, COLOR_ACCENT);
        Image image = new ImageIcon(logoUrl).getImage();
        int height = Math.max(1, (int) Math.round(width * image.getHeight(null) / (double) image.getWidth(null)));
        JLabel label = new JLabel(new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH)));
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        return label;
    }

    public static JLabel createLabel(String text, float size, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(getFont(Font.BOLD, size));
        l.setForeground(color);
        return l;
    }

    public static JButton createStyledButton(String text) {
        JButton b = new JButton(text);
        styleButton(b, true);
        b.setPreferredSize(new Dimension(190, 46));
        b.setMaximumSize(new Dimension(240, 48));
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    /** 보조 동작에 쓰는 공통 버튼을 생성합니다. */
    public static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, false);
        button.setPreferredSize(new Dimension(190, 46));
        button.setMaximumSize(new Dimension(240, 48));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    /** 취소·로그아웃처럼 주의를 환기해야 하는 동작용 버튼을 만듭니다. */
    public static JButton createDangerButton(String text) {
        JButton button = new JButton(text);
        styleButton(button, new Color(177, 58, 69), Color.WHITE, new Color(218, 91, 101));
        button.setPreferredSize(new Dimension(190, 46));
        button.setMaximumSize(new Dimension(240, 48));
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        return button;
    }

    /** primary 여부에 따라 공통 색상, 글꼴, 여백과 마우스 상태를 설정합니다. */
    public static void styleButton(JButton button, boolean primary) {
        Color base = primary ? COLOR_ACCENT : COLOR_SURFACE_RAISED;
        Color foreground = primary ? new Color(20, 31, 42) : COLOR_TEXT;
        Color borderColor = primary ? COLOR_ACCENT : COLOR_BORDER;
        styleButton(button, base, foreground, borderColor);
    }

    private static void styleButton(JButton button, Color base, Color foreground, Color borderColor) {
        Border border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderColor, 1),
                BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setFont(getFont(Font.BOLD, 14f));
        button.setForeground(foreground);
        button.setBackground(base);
        button.setBorder(border);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setFocusPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Object previousListener = button.getClientProperty("tetris.ui.mouseListener");
        if (previousListener instanceof MouseAdapter) button.removeMouseListener((MouseAdapter) previousListener);
        MouseAdapter interaction = new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(base.brighter());
            }
            @Override public void mouseExited(MouseEvent e) {
                button.setBackground(base);
            }
            @Override public void mousePressed(MouseEvent e) {
                if (button.isEnabled()) button.setBackground(base.darker());
            }
            @Override public void mouseReleased(MouseEvent e) {
                button.setBackground(button.contains(e.getPoint()) ? base.brighter() : base);
            }
        };
        button.addMouseListener(interaction);
        button.putClientProperty("tetris.ui.mouseListener", interaction);
    }

    public static JButton createHeaderButton(String text) {
        return createSecondaryButton(text);
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
        p.setBackground(COLOR_SURFACE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDER, 1),
                BorderFactory.createEmptyBorder(20, 25, 20, 25)
        ));
        p.setPreferredSize(new Dimension(w, h));
        return p;
    }

    /** 전체 화면 오버레이 안의 팝업 상자를 아래에서 중앙으로 부드럽게 올립니다. */
    public static void animatePopupIn(JPanel popup) {
        if (popup == null || popup.getComponentCount() == 0) return;
        popup.setLayout(null);
        Component box = popup.getComponent(0);
        installPopupRecentering(popup, box);
        Object oldTimer = popup.getClientProperty("tetris.popup.animation");
        if (oldTimer instanceof javax.swing.Timer) ((javax.swing.Timer) oldTimer).stop();
        popup.putClientProperty("tetris.popup.animating", Boolean.TRUE);
        Dimension initialSize = fittedPopupSize(popup, box);
        box.setBounds((popup.getWidth() - initialSize.width) / 2, popup.getHeight(),
                initialSize.width, initialSize.height);
        long start = System.nanoTime();
        javax.swing.Timer timer = new javax.swing.Timer(15, event -> {
            double progress = Math.min(1.0, (System.nanoTime() - start) / 260_000_000.0);
            double eased = 1.0 - Math.pow(1.0 - progress, 3);
            Dimension boxSize = fittedPopupSize(popup, box);
            int targetX = (popup.getWidth() - boxSize.width) / 2;
            int targetY = (popup.getHeight() - boxSize.height) / 2;
            int startY = popup.getHeight();
            int y = (int) Math.round(startY + (targetY - startY) * eased);
            box.setBounds(targetX, y, boxSize.width, boxSize.height);
            popup.repaint();
            if (progress >= 1.0) {
                ((javax.swing.Timer) event.getSource()).stop();
                popup.putClientProperty("tetris.popup.animating", Boolean.FALSE);
                centerPopupBox(popup, box);
            }
        });
        popup.putClientProperty("tetris.popup.animation", timer);
        // 시작 위치를 먼저 지정해 팝업이 중앙에 한 프레임 나타났다가 이동하는 현상을 막습니다.
        popup.setVisible(true);
        timer.start();
    }

    /** 창 크기가 바뀌면 열려 있는 팝업 상자를 새 중앙 좌표로 옮깁니다. */
    private static void installPopupRecentering(JPanel popup, Component box) {
        if (Boolean.TRUE.equals(popup.getClientProperty("tetris.popup.recenterInstalled"))) return;
        popup.putClientProperty("tetris.popup.recenterInstalled", Boolean.TRUE);
        popup.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent event) {
                if (!Boolean.TRUE.equals(popup.getClientProperty("tetris.popup.animating"))) {
                    centerPopupBox(popup, box);
                }
            }
        });
    }

    private static Dimension fittedPopupSize(JPanel popup, Component box) {
        Dimension preferred = box.getPreferredSize();
        if (preferred.width <= 0 || preferred.height <= 0) preferred = new Dimension(400, 200);
        return new Dimension(Math.min(preferred.width, Math.max(200, popup.getWidth() - 40)),
                Math.min(preferred.height, Math.max(160, popup.getHeight() - 40)));
    }

    private static void centerPopupBox(JPanel popup, Component box) {
        Dimension size = fittedPopupSize(popup, box);
        box.setBounds((popup.getWidth() - size.width) / 2,
                (popup.getHeight() - size.height) / 2, size.width, size.height);
        popup.revalidate();
        popup.repaint();
    }
}
