package game.ui.outgame.popup;

import game.NetworkManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

public class AuthPopup extends JPanel {
    private MainFrame mainFrame;
    private JPanel popupBox;
    private CardLayout cardLayout = new CardLayout();
    private JPanel cardsPanel = new JPanel(cardLayout);
    public static final String CARD_LOGIN = "LOGIN";
    public static final String CARD_REGISTER = "REGISTER";
    private JTextField loginIdField;
    private JPasswordField loginPwField;
    private JTextField regIdField;
    private JPasswordField regPwField;
    private JPasswordField regPwConfirmField;
    private JLabel regStatusLabel; // 아이디/비밀번호 검증 결과 안내 라벨
    private JLabel loginStatusLabel;

    //==================================================================================================================
    //로그인/회원가입 창 띄워주는 코드
    //==================================================================================================================
    public AuthPopup(MainFrame mainFrame) {
        //필요한 설정 모아둠과 동시에 gridbagcontains가 popupbox를 화면중앙으로 잡아줌
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(false);
        popupBox = UIUtils.createBasePopupBox(500, 380); //팝업 박스 사이즈 설정
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
    }

    //==================================================================================================================
    //로그인 창 띄워주는 코드 (스케치 UI 세로 배치 적용)
    //==================================================================================================================
    private JPanel createLoginCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        //제목
        JLabel titleLabel = UIUtils.createLabel("로그인", 22f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        //입력폼
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

        //오류메세지 라벨
        loginStatusLabel = UIUtils.createLabel(" ", 12f, new Color(255, 120, 120));
        loginStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginStatusLabel.setMaximumSize(new Dimension(300, 20)); // 폼/버튼 너비(300px)와 동일하게 통일
        //오류메세지 위치고정
        Dimension statusSize = new Dimension(300, 20);
        loginStatusLabel.setPreferredSize(statusSize);
        loginStatusLabel.setMinimumSize(statusSize);
        loginStatusLabel.setMaximumSize(statusSize);

        //버튼 생성
        JButton loginBtn = UIUtils.createStyledButton("로그인");
        JButton regSwitchBtn = UIUtils.createStyledButton("회원가입");
        JButton closeBtn = UIUtils.createStyledButton("닫기");

        //버튼 역할부여
        loginBtn.addActionListener(e -> handleLogin());
        regSwitchBtn.addActionListener(e -> {resetRegisterForm();cardLayout.show(cardsPanel, CARD_REGISTER);});
        closeBtn.addActionListener(e -> mainFrame.hideAuthPopup());

        //버튼 위치고정
        Dimension btnSize = new Dimension(300, 42);
        for (JButton btn : new JButton[]{loginBtn, regSwitchBtn,closeBtn}) {
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
            btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        }

        //배열하기
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
    //로그인 기능을 하는 코드
    //==================================================================================================================
    private void handleLogin() {
        String id = loginIdField.getText().trim();
        String pw = new String(loginPwField.getPassword()).trim();
        NetworkManager net = NetworkManager.getInstance();
        Color errorColor = new Color(255, 120, 120);

        // 로그인 할때 나오는 오류
        if (id.isEmpty()) {
            showLoginStatus("아이디를 입력해 주세요.", errorColor);
            return;
        }else if (pw.isEmpty()) {
            showLoginStatus("비밀번호를 입력해 주세요.", errorColor);
            return;
        }

        // 서버 연결 후 로그인
        if (!net.isConnected) {
            boolean connected = net.connect(NetworkManager.SERVER_HOST, NetworkManager.SERVER_PORT);
            if (!connected) {
                showLoginStatus("서버에 연결할 수 없습니다.", errorColor);
                return;
            }
        }
        showLoginStatus(" ", Color.WHITE); // 성공 시 메시지 초기화
        net.requestLogin(id, pw);
    }

    //==================================================================================================================
    //회원가입 창 띄워주는 코드
    //==================================================================================================================
    private JPanel createRegisterCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setOpaque(false);

        //회원가입 창 제목
        JLabel titleLabel = UIUtils.createLabel("회원가입", 22f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        //회원가입 입력폼
        JPanel form = new JPanel(new GridLayout(3, 2, 8, 12));
        form.setOpaque(false);
        form.setMaximumSize(new Dimension(340, 110));
        form.setAlignmentX(Component.CENTER_ALIGNMENT);

        regIdField = new JTextField();
        regPwField = new JPasswordField();
        regPwConfirmField = new JPasswordField();

        // 포커스 탈출 시 중복 체크 안내
        regIdField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                String id = regIdField.getText().trim();
                if (!id.isEmpty()) {
                    showRegStatus("", Color.CYAN);
                }
            }
        });

        //회원가입시 사용할 폼 추가
        form.add(UIUtils.createLabel("회원가입할 아이디:", 13f, Color.WHITE));
        form.add(regIdField);
        form.add(UIUtils.createLabel("사용할 비밀번호:", 13f, Color.WHITE));
        form.add(regPwField);
        form.add(UIUtils.createLabel("비밀번호 확인:", 13f, Color.WHITE));
        form.add(regPwConfirmField);

        // 상태 표시 라벨
        regStatusLabel = UIUtils.createLabel(" ", 12f, new Color(255, 120, 120));
        regStatusLabel.setHorizontalAlignment(SwingConstants.CENTER); // 글자 자체 중앙 정렬
        regStatusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);     // 상자 중앙 정렬

        //상태 표시 라벨 위치고정
        Dimension statusSize = new Dimension(340, 20);
        regStatusLabel.setPreferredSize(statusSize);
        regStatusLabel.setMinimumSize(statusSize);
        regStatusLabel.setMaximumSize(statusSize);

        //버튼 생성
        JButton submitBtn = UIUtils.createStyledButton("만들기");
        JButton backBtn = UIUtils.createStyledButton("뒤로가기");

        //버튼 위치고정
        Dimension btnSize = new Dimension(120, 40);
        for (JButton btn : new JButton[]{submitBtn, backBtn}) {
            btn.setPreferredSize(btnSize);
            btn.setMaximumSize(btnSize);
        }

        //버튼 역할부여
        submitBtn.addActionListener(e -> handleRegister());
        backBtn.addActionListener(e -> cardLayout.show(cardsPanel, CARD_LOGIN));

        //버튼 레이아웃
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        btnPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnPanel.setMaximumSize(new Dimension(340, 40));
        btnPanel.add(submitBtn);
        btnPanel.add(backBtn);

        //버튼 위치수정
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
    //회원가입 기능을 하는 코드
    //==================================================================================================================
    private void handleRegister() {
        String id = regIdField.getText().trim();
        String pw = new String(regPwField.getPassword()).trim();
        String pwConfirm = new String(regPwConfirmField.getPassword()).trim();
        NetworkManager net = NetworkManager.getInstance();
        Color errorColor = new Color(255, 120, 120);

        //회원가입 시 발생할 수 있는 오류 체크
        if (id.isEmpty()) {
            showRegStatus("아이디를 입력해 주세요.", errorColor);
            return;
        }
        if (pw.isEmpty()) {
            showRegStatus("비밀번호를 입력해 주세요.", errorColor);
            return;
        }
        if (!pw.equals(pwConfirm)) {
            showRegStatus("비밀번호가 일치하지 않습니다.", errorColor);
            return;
        }

        // 서버 접속 안내 메세지
        showLoginStatus("서버에 연결하는 중", Color.CYAN);

        // 백그라운드 스레드에서 네트워크 작업 수행 (UI 프리징 방지) <- 이거 해도 여전히 ui 프리징 걸림
        new Thread(() -> {

            if (!net.isConnected) {
                boolean connected = net.connect(NetworkManager.SERVER_HOST, NetworkManager.SERVER_PORT);

                if (!connected) {
                    javax.swing.SwingUtilities.invokeLater(() ->
                            showLoginStatus("서버에 연결할 수 없습니다.", errorColor)
                    );
                    return;
                }
            }

            // 연결 성공 또는 이미 연결된 상태면 로그인 요청 전송
            javax.swing.SwingUtilities.invokeLater(() -> showLoginStatus("로그인 요청 중...", Color.WHITE));
            net.requestRegister(id, pw, id); //일단 아이디가 닉네임입니다
        }).start();
    }

    //==================================================================================================================
    //사용하는 메서드
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

    @Override //뒤에 배경 반투명...
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}