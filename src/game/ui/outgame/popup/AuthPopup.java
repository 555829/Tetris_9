package game.ui.outgame.popup;

import game.NetworkManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;

//======================================================================================================================
// 로그인이랑 회원가입 팝업을 담당하는 코드
//======================================================================================================================
public class AuthPopup extends JPanel {
    private MainFrame mainFrame;
    private JPanel popupBox;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardsPanel = new JPanel(cardLayout);
    private JTextField loginIdField;
    private JPasswordField loginPwField;
    private JTextField regIdField;
    private JPasswordField regPwField;
    private JPasswordField regPwConfirmField;
    private JLabel regStatusLabel;
    private JLabel loginStatusLabel;
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";

    //==================================================================================================================
    // 로그인/회원가입 창 띄워주는 코드
    //==================================================================================================================
    public AuthPopup(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(false);
        popupBox = UIUtils.createBasePopupBox(500, 380);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(popupBox, gbc);
        popupBox.setLayout(new BorderLayout());
        cardsPanel.setOpaque(false);
        cardsPanel.add(createLoginCard(), CARD_LOGIN);
        cardsPanel.add(createRegisterCard(), CARD_REGISTER);
        popupBox.add(cardsPanel, BorderLayout.CENTER);

        NetworkManager.getInstance().addPacketListener(this::onPacketReceived);
    }

    //==================================================================================================================
    // 패킷처리
    //==================================================================================================================
    private void onPacketReceived(String packet) {
        String[] tokens = packet.split("\\|", -1);
        if (tokens.length < 2) return;
        String cmd = tokens[0];

        // 1. 로그인 응답 처리
        if ("RES_LOGIN".equals(cmd)) {
            String result = tokens[1];

            if ("SUCCESS".equals(result)) {
                showLoginStatus(" ", Color.WHITE);

                // 팝업 닫기 및 MultiLobbyPanel로 이동!
                mainFrame.hideAuthPopup();
                mainFrame.showScreen(MainFrame.TAG_MULTI_LOBBY);
            } else {
                String failReason = tokens.length > 2 ? tokens[2] : "로그인 정보가 올바르지 않습니다.";
                showLoginStatus(failReason, new Color(255, 120, 120));
            }
        }
        // 2. 회원가입 응답 처리
        else if ("RES_REGISTER".equals(cmd)) {
            String result = tokens[1];

            if ("SUCCESS".equals(result)) {
                showRegStatus("회원가입에 성공했습니다!", Color.CYAN);
                resetRegisterForm();
                cardLayout.show(cardsPanel, CARD_LOGIN); // 로그인 화면으로 이동
                showLoginStatus("회원가입 성공! 로그인해 주세요.", Color.CYAN);
            } else {
                String failReason = tokens.length > 2 ? tokens[2] : "회원가입에 실패했습니다.";
                showRegStatus(failReason, new Color(255, 120, 120));
            }
        }
    }

    //==================================================================================================================
    // 로그인 UI
    //==================================================================================================================
    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        JLabel titleLabel = UIUtils.createLabel("로그인", 22f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setOpaque(false);
        formPanel.setMaximumSize(new Dimension(300, 70));
        formPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        loginIdField = new JTextField();
        loginPwField = new JPasswordField();
        formPanel.add(UIUtils.createLabel("아이디", 14f, Color.WHITE));
        formPanel.add(loginIdField);
        formPanel.add(UIUtils.createLabel("비밀번호", 14f, Color.WHITE));
        formPanel.add(loginPwField);

        loginStatusLabel = UIUtils.createLabel(" ", 12f, new Color(255, 120, 120));
        loginStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        Dimension statusSize = new Dimension(300, 20);
        loginStatusLabel.setPreferredSize(statusSize);
        loginStatusLabel.setMinimumSize(statusSize);
        loginStatusLabel.setMaximumSize(statusSize);

        JButton loginBtn = UIUtils.createStyledButton("로그인");
        JButton regSwitchBtn = UIUtils.createSecondaryButton("회원가입");
        JButton closeBtn = UIUtils.createSecondaryButton("닫기");

        loginBtn.addActionListener(e -> handleLogin());
        regSwitchBtn.addActionListener(e -> { resetRegisterForm(); cardLayout.show(cardsPanel, CARD_REGISTER); });
        closeBtn.addActionListener(e -> mainFrame.hideAuthPopup());

        Dimension btnSize = new Dimension(300, 42);
        for (JButton btn : new JButton[]{loginBtn, regSwitchBtn, closeBtn}) {
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        card.add(Box.createVerticalStrut(15));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(formPanel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginStatusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(loginBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(regSwitchBtn);
        card.add(Box.createVerticalStrut(10));
        card.add(closeBtn);
        return card;
    }

    //==================================================================================================================
    // 로그인 처리
    //==================================================================================================================
    private void handleLogin() {
        String id = loginIdField.getText().trim();
        String pw = new String(loginPwField.getPassword());
        NetworkManager net = NetworkManager.getInstance();
        Color errorColor = new Color(255, 120, 120);

        if (id.isEmpty()) {
            showLoginStatus("아이디를 입력해 주세요.", errorColor);
            return;
        } else if (pw.isEmpty()) {
            showLoginStatus("비밀번호를 입력해 주세요.", errorColor);
            return;
        }
        if (!isValidProtocolField(id, 64) || !isValidProtocolField(pw, 128)) {
            showLoginStatus("아이디와 비밀번호는 구분자 없이 지정 길이 이내로 입력해 주세요.", errorColor);
            return;
        }

        showLoginStatus("서버에 연결하는 중", Color.CYAN);

        // 이걸 하면 ui 처리하는 부분과 로그인하는 쪽의 스레드를 나눠서 ui가 멈추지 않음
        new Thread(() -> {
            if (!net.isConnected) {
                boolean connected = net.connect(NetworkManager.SERVER_HOST, NetworkManager.SERVER_PORT);
                if (!connected) {
                    SwingUtilities.invokeLater(() -> showLoginStatus("서버에 연결할 수 없습니다.", errorColor));
                    return;
                }
            }

            SwingUtilities.invokeLater(() -> showLoginStatus("로그인 요청 중...", Color.WHITE));
            net.requestLogin(id, pw);
        }).start();
    }

    //==================================================================================================================
    // 회원가입 UI
    //==================================================================================================================
    private JPanel createRegisterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        JLabel titleLabel = UIUtils.createLabel("회원가입", 22f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(340, 110));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        regIdField = new JTextField();
        regPwField = new JPasswordField();
        regPwConfirmField = new JPasswordField();

        form.add(UIUtils.createLabel("회원가입할 아이디:", 13f, Color.WHITE));
        form.add(regIdField);
        form.add(UIUtils.createLabel("사용할 비밀번호:", 13f, Color.WHITE));
        form.add(regPwField);
        form.add(UIUtils.createLabel("비밀번호 확인:", 13f, Color.WHITE));
        form.add(regPwConfirmField);

        regStatusLabel = UIUtils.createLabel(" ", 12f, new Color(255, 120, 120));
        regStatusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        regStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        Dimension statusSize = new Dimension(340, 20);
        regStatusLabel.setPreferredSize(statusSize);
        regStatusLabel.setMinimumSize(statusSize);
        regStatusLabel.setMaximumSize(statusSize);

        JButton submitBtn = UIUtils.createStyledButton("만들기");
        JButton backBtn = UIUtils.createSecondaryButton("뒤로가기");

        Dimension btnSize = new Dimension(120, 40);
        submitBtn.setPreferredSize(btnSize);
        submitBtn.setMaximumSize(btnSize);
        backBtn.setPreferredSize(btnSize);
        backBtn.setMaximumSize(btnSize);

        submitBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> cardLayout.show(cardsPanel, CARD_LOGIN));

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(340, 40));
        btnPanel.add(submitBtn);
        btnPanel.add(backBtn);

        card.add(Box.createVerticalStrut(10));
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(20));
        card.add(form);
        card.add(Box.createVerticalStrut(8));
        card.add(regStatusLabel);
        card.add(Box.createVerticalStrut(8));
        card.add(btnPanel);
        return card;
    }

    //==================================================================================================================
    // 회원가입 처리 (왜 되는건지 모르겠음...)
    //==================================================================================================================
    private void handleRegister() {
        String id = regIdField.getText().trim();
        String pw = new String(regPwField.getPassword());
        String pwConfirm = new String(regPwConfirmField.getPassword());
        NetworkManager net = NetworkManager.getInstance();
        Color errorColor = new Color(255, 120, 120);

        if (id.isEmpty()) {
            showRegStatus("아이디를 입력해 주세요.", errorColor);
            return;
        }
        if (pw.isEmpty()) {
            showRegStatus("비밀번호를 입력해 주세요.", errorColor);
            return;
        }
        if (!isValidProtocolField(id, 30) || id.indexOf(',') >= 0 || !isValidProtocolField(pw, 128)) {
            showRegStatus("아이디는 30자 이내, 비밀번호는 128자 이내로 입력하고 구분자나 쉼표는 사용할 수 없습니다.", errorColor);
            return;
        }
        if (!pw.equals(pwConfirm)) {
            showRegStatus("비밀번호가 일치하지 않습니다.", errorColor);
            return;
        }

        showRegStatus("서버에 연결하는 중", Color.CYAN);

        new Thread(() -> {
            if (!net.isConnected) {
                boolean connected = net.connect(NetworkManager.SERVER_HOST, NetworkManager.SERVER_PORT);
                if (!connected) {
                    SwingUtilities.invokeLater(() -> showRegStatus("서버에 연결할 수 없습니다.", errorColor));
                    return;
                }
            }

            SwingUtilities.invokeLater(() -> showRegStatus("회원가입 요청 중", Color.WHITE));
            net.requestRegister(id, pw, id);
            //닉네임 따로 받는거 깜빡했는데 수정하기 귀찮아서 그냥 id가 닉네임... 로비안에 들어가서 프로필 버튼 누르면
            //수정할수 있습니다
        }).start();
    }

    //==================================================================================================================
    // 필요한 부품들
    //==================================================================================================================
    private void showLoginStatus(String message, Color color) {
        loginStatusLabel.setText(message);
        loginStatusLabel.setForeground(color);
    }

    private void showRegStatus(String message, Color color) {
        regStatusLabel.setText(message);
        regStatusLabel.setForeground(color);
    }

    private void resetRegisterForm() {
        regIdField.setText("");
        regPwField.setText("");
        regPwConfirmField.setText("");
        regStatusLabel.setText(" ");
    }

    private static boolean isValidProtocolField(String value, int maxLength) {
        if (value == null || value.isEmpty() || value.length() > maxLength) return false;
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            if (ch == '|' || Character.isISOControl(ch)) return false;
        }
        return true;
    }

    @Override //팝업 이후 반투명 해주는코드
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
