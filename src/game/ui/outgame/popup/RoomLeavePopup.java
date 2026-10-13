package game.ui.outgame.popup;

import game.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

//======================================================================================================================
// 방에서 나갈때 팝업을 띄워줌
//======================================================================================================================
public class RoomLeavePopup extends JPanel {
    private static final int BOX_WIDTH = 430;
    private static final int BOX_HEIGHT = 170;
    private final JPanel popupBox = new JPanel(new BorderLayout(12, 18));
    private final Timer animationTimer;
    private final JLabel titleLabel = UIUtils.createLabel("방에서 나가시겠습니까?", 21f, Color.WHITE);
    private Runnable leaveAction = () -> { };
    private long animationStartedAt;

    public RoomLeavePopup(Runnable cancelAction) {
        setOpaque(false);
        setLayout(null);

        popupBox.setBackground(new Color(39, 43, 57));
        popupBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(104, 124, 166), 2),
                new EmptyBorder(22, 26, 22, 26)));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton leave = UIUtils.createDangerButton("나가기");
        JButton stay = UIUtils.createSecondaryButton("계속하기");
        leave.setPreferredSize(new Dimension(150, 44));
        stay.setPreferredSize(new Dimension(150, 44));
        leave.addActionListener(e -> {
            cancelAction.run();
            leaveAction.run();
        });
        stay.addActionListener(e -> cancelAction.run());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        actions.add(leave);
        actions.add(stay);

        popupBox.add(titleLabel, BorderLayout.CENTER);
        popupBox.add(actions, BorderLayout.SOUTH);
        add(popupBox);
        animationTimer = new Timer(15, e -> animateFrame());
    }

    public void showPopup(Runnable leaveAction) {
        this.leaveAction = leaveAction == null ? () -> { } : leaveAction;
        setVisible(true);
        animationStartedAt = System.nanoTime();
        animationTimer.start();
    }

    public void hidePopup() {
        animationTimer.stop();
        setVisible(false);
    }

    private void animateFrame() {
        double progress = Math.min(1.0, (System.nanoTime() - animationStartedAt) / 260_000_000.0);
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        int targetY = (getHeight() - BOX_HEIGHT) / 2;
        int startY = getHeight();
        int y = (int) Math.round(startY + (targetY - startY) * eased);
        popupBox.setBounds((getWidth() - BOX_WIDTH) / 2, y, BOX_WIDTH, BOX_HEIGHT);
        if (progress >= 1.0) animationTimer.stop();
        repaint();
    }

    @Override
    public void doLayout() {
        if (!animationTimer.isRunning()) {
            popupBox.setBounds((getWidth() - BOX_WIDTH) / 2, (getHeight() - BOX_HEIGHT) / 2,
                    BOX_WIDTH, BOX_HEIGHT);
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(new Color(0, 0, 0, 175));
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }
}
