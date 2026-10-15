package game.ui.outgame.popup;

import game.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.BiConsumer;

//======================================================================================================================
// 방 생성 요청을 하는 팝업을 띄워줍니다
//======================================================================================================================
public class RoomCreatePopup extends JPanel {
    private static final int BOX_WIDTH = 460;
    private static final int BOX_HEIGHT = 330;

    private final JPanel box = new JPanel(new BorderLayout(12, 16));
    private final JTextField titleField = new JTextField();
    private final JComboBox<String> modeSelector = new JComboBox<>(
            new String[]{"시간전 (90초)", "클래식 (40줄)"});
    private final JLabel errorLabel = new JLabel(" ", SwingConstants.CENTER);
    private final Timer animationTimer;
    private long animationStartedAt;
    private BiConsumer<String, String> createAction = (title, mode) -> { };

    public RoomCreatePopup(Runnable closeAction) {
        setOpaque(false);
        setLayout(null);

        box.setBackground(UIUtils.COLOR_SURFACE);
        box.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER, 2),
                new EmptyBorder(22, 26, 22, 26)));

        JLabel heading = UIUtils.createLabel("방 만들기", 22f, UIUtils.COLOR_TEXT);
        heading.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel fields = new JPanel(new GridLayout(4, 1, 0, 7));
        fields.setOpaque(false);
        fields.add(fieldLabel("방 제목"));
        titleField.setFont(UIUtils.getBodyFont(Font.PLAIN, 15f));
        titleField.setBackground(UIUtils.COLOR_BACKGROUND);
        titleField.setForeground(UIUtils.COLOR_TEXT);
        titleField.setCaretColor(UIUtils.COLOR_ACCENT);
        titleField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(7, 9, 7, 9)));
        fields.add(titleField);
        fields.add(fieldLabel("게임 모드"));
        modeSelector.setFont(UIUtils.getBodyFont(Font.PLAIN, 14f));
        modeSelector.setBackground(UIUtils.COLOR_SURFACE_RAISED);
        modeSelector.setForeground(UIUtils.COLOR_TEXT);
        fields.add(modeSelector);

        errorLabel.setForeground(new Color(255, 135, 145));
        errorLabel.setFont(UIUtils.getBodyFont(Font.PLAIN, 12f));

        JPanel center = new JPanel(new BorderLayout(0, 8));
        center.setOpaque(false);
        center.add(fields, BorderLayout.CENTER);
        center.add(errorLabel, BorderLayout.SOUTH);

        JButton createButton = UIUtils.createStyledButton("만들기");
        JButton cancelButton = UIUtils.createSecondaryButton("취소");
        createButton.setPreferredSize(new Dimension(150, 42));
        cancelButton.setPreferredSize(new Dimension(150, 42));
        createButton.addActionListener(e -> submit());
        cancelButton.addActionListener(e -> closeAction.run());
        titleField.addActionListener(e -> submit());
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        actions.setOpaque(false);
        actions.add(createButton);
        actions.add(cancelButton);

        box.add(heading, BorderLayout.NORTH);
        box.add(center, BorderLayout.CENTER);
        box.add(actions, BorderLayout.SOUTH);
        add(box);
        animationTimer = new Timer(15, e -> animateFrame());
    }

    public void showPopup(BiConsumer<String, String> createAction) {
        this.createAction = createAction == null ? (title, mode) -> { } : createAction;
        titleField.setText("");
        errorLabel.setText(" ");
        setVisible(true);
        animationStartedAt = System.nanoTime();
        animationTimer.start();
        SwingUtilities.invokeLater(titleField::requestFocusInWindow);
    }

    public void hidePopup() {
        animationTimer.stop();
        setVisible(false);
    }

    private void submit() {
        String title = titleField.getText().trim();
        if (title.isEmpty()) {
            errorLabel.setText("방 제목을 입력해 주세요.");
            titleField.requestFocusInWindow();
            return;
        }
        if (title.length() > 30 || title.indexOf('|') >= 0 || title.indexOf(',') >= 0
                || containsControlCharacter(title)) {
            errorLabel.setText("방 제목은 30자 이내이며 쉼표와 구분자를 사용할 수 없습니다.");
            titleField.requestFocusInWindow();
            return;
        }
        createAction.accept(title, String.valueOf(modeSelector.getSelectedItem()));
    }

    private static boolean containsControlCharacter(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (Character.isISOControl(value.charAt(i))) return true;
        }
        return false;
    }

    private static JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(UIUtils.COLOR_MUTED);
        label.setFont(UIUtils.getBodyFont(Font.BOLD, 13f));
        return label;
    }

    private void animateFrame() {
        double progress = Math.min(1.0, (System.nanoTime() - animationStartedAt) / 260_000_000.0);
        double eased = 1.0 - Math.pow(1.0 - progress, 3);
        int targetY = (getHeight() - BOX_HEIGHT) / 2;
        int y = (int) Math.round(getHeight() + (targetY - getHeight()) * eased);
        box.setBounds((getWidth() - BOX_WIDTH) / 2, y, BOX_WIDTH, BOX_HEIGHT);
        if (progress >= 1.0) animationTimer.stop();
        repaint();
    }

    @Override
    public void doLayout() {
        if (!animationTimer.isRunning()) {
            box.setBounds((getWidth() - BOX_WIDTH) / 2, (getHeight() - BOX_HEIGHT) / 2,
                    BOX_WIDTH, BOX_HEIGHT);
        }
    }

    @Override //이거도 반투명 띄워주는 코드
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        graphics.setColor(new Color(0, 0, 0, 175));
        graphics.fillRect(0, 0, getWidth(), getHeight());
    }
}
