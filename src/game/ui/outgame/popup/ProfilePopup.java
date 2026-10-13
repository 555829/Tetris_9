package game.ui.outgame.popup;

import game.NetworkManager;
import game.profile.ProfileImageCodec;
import game.ui.MainFrame;
import game.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

//======================================================================================================================
// 프로필을 띄워주는 팝업 (프로필카드)
//======================================================================================================================
public class ProfilePopup extends JPanel {
    private MainFrame mainFrame;
    private Image bgImage;

    // 유저 프로필 데이터 변수
    private String nickname = "";
    private int playCount ;
    private String createdAt = "";
    private int money ;

    // UI 컴포넌트
    private JLabel nickLabel;
    private JLabel profileImgLabel;
    private JLabel editHintLabel;
    private JLabel playCountLabel;
    private JLabel createAtLabel;
    private JPanel cardBox;

    public ProfilePopup(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new GridBagLayout());
        setOpaque(false);

        // 1. 카드 UI 레이아웃 패널 생성
        cardBox = createImageBackgroundCardBox();

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(cardBox, gbc);

        // 2. 이벤트 리스너 등록
        setupHoverAndEditEvents();

        // 3. 현재 저장된 유저 정보 기반 UI 바인딩
        refreshData();
        NetworkManager.getInstance().addPacketListener(this::handleNicknameResponse);
    }

    /**
     * NetworkManager의 현재 데이터를 기반으로 화면 컴포넌트를 갱신합니다.
     */
    /** 네트워크 프로필 상태를 다시 읽어 카드, 닉네임, 전적과 사진을 화면에 반영합니다. */
    public void refreshData() {
        NetworkManager net = NetworkManager.getInstance();

        // 메모리에 저장된 데이터 가져오기
        this.nickname = net.getMyNickname();
        this.playCount = net.getPlayCount();
        this.createdAt = net.getCreatedAt();
        this.money = net.getMoney();

        // 1. 장착 중인 카드 스킨 배경 이미지 로드
        loadBackgroundImage(net.getEquippedSkin());

        // 2. 텍스트 라벨 업데이트
        if (nickLabel != null) {
            nickLabel.setText((nickname == null || nickname.trim().isEmpty()) ? "미인증" : nickname);
        }
        if (playCountLabel != null) {
            playCountLabel.setText(playCount + "");
        }

        if (createAtLabel != null) {
            createAtLabel.setText(createdAt + "부터 함께함 ");
        }

        // 3. 프로필 이미지 업데이트 (Blob -> Base64 -> ImageIcon)
        ImageIcon pic = net.getProfilePic();
        if (pic != null && profileImgLabel != null) {
            Image scaledImage = ProfileImageCodec.centerCropToSquare(pic.getImage())
                    .getScaledInstance(82, 82, Image.SCALE_SMOOTH);
            profileImgLabel.setIcon(new ImageIcon(scaledImage));
            profileImgLabel.setText("");
        } else if (profileImgLabel != null) {
            profileImgLabel.setIcon(loadProfileImage("/game/asset/default_profile.png", 82, 82));
            profileImgLabel.setText("");
        }

        revalidate();
        repaint();
    }

    // 장착 스킨에 따라 배경 이미지 로드
    private void loadBackgroundImage(String skinName) {
        if (skinName == null) skinName = "WHITE";

        String skinPath = "/game/asset/profile_card_bg_" + skinName.toLowerCase() + ".png";
        java.net.URL imgURL = getClass().getResource(skinPath);

        // 전용 프로필 배경이 없으면 상점에서 사용하는 해당 스킨 이미지를 적용
        if (imgURL == null) {
            imgURL = getClass().getResource("/game/asset/card_" + skinName.toLowerCase() + ".png");
        }

        // 해당 카드 이미지도 없는 경우 기본 배경 로드
        if (imgURL == null) {
            imgURL = getClass().getResource("/game/asset/profile_card_bg.png");
        }

        if (imgURL != null) {
            bgImage = new ImageIcon(imgURL).getImage();
        }
    }

    private JPanel createImageBackgroundCardBox() {
        int cardW = 480;
        int cardH = 370;

        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int bgY = 0;

                if (bgImage != null) {
                    g2.drawImage(bgImage, 0, bgY, cardW, 290, this);
                } else {
                    g2.setColor(new Color(30, 35, 50, 240));
                    g2.fillRoundRect(0, bgY, cardW, 290, 20, 20);
                }

                g2.dispose();
            }
        };

        card.setLayout(null);
        card.setPreferredSize(new Dimension(cardW, cardH));
        card.setOpaque(false);

        // --------------------------------------------------------------------------------------------------------------
        // 1. [중앙 '판' 글자 오른쪽] 판수 라벨 (검은색, 큰 폰트, '판' 이미지 옆 위치 보정)
        // --------------------------------------------------------------------------------------------------------------
        playCountLabel = UIUtils.createLabel(String.valueOf(playCount), 40f, Color.BLACK);
        playCountLabel.setBounds(120, 190, 200, 40);
        card.add(playCountLabel);

        // --------------------------------------------------------------------------------------------------------------
        // 1. [중앙 '판' 글자 오른쪽] 판수 라벨 (검은색, 큰 폰트, '판' 이미지 옆 위치 보정)
        // --------------------------------------------------------------------------------------------------------------
        createAtLabel = UIUtils.createLabel(String.valueOf(createdAt), 12f, Color.BLACK);
        createAtLabel.setBounds(180, 240, 200, 40);
        card.add(createAtLabel);

        // --------------------------------------------------------------------------------------------------------------
        // 2. 프로필 사진 영역
        // --------------------------------------------------------------------------------------------------------------
        JPanel imgContainer = new JPanel(null);
        imgContainer.setBounds(322, 68, 82, 82);
        imgContainer.setOpaque(false);

        profileImgLabel = new JLabel();
        profileImgLabel.setBounds(0, 0, 82, 82);
        profileImgLabel.setOpaque(true);
        profileImgLabel.setBackground(new Color(50, 55, 75, 180));
        profileImgLabel.setBorder(new LineBorder(new Color(255, 255, 255, 180), 2));
        imgContainer.add(profileImgLabel);

        editHintLabel = new JLabel("✏️ 수정", SwingConstants.CENTER);
        editHintLabel.setBounds(0, 0, 82, 82);
        editHintLabel.setOpaque(true);
        editHintLabel.setBackground(new Color(0, 0, 0, 170));
        editHintLabel.setForeground(Color.CYAN);
        editHintLabel.setFont(editHintLabel.getFont().deriveFont(Font.BOLD, 12f));
        editHintLabel.setVisible(false);
        imgContainer.add(editHintLabel);
        imgContainer.setComponentZOrder(editHintLabel, 0);

        card.add(imgContainer);

        // --------------------------------------------------------------------------------------------------------------
        // 3. 프로필 사진 아래 닉네임 (검은색으로 변경)
        // --------------------------------------------------------------------------------------------------------------
        nickLabel = UIUtils.createLabel("", 14f, Color.BLACK);
        nickLabel.setHorizontalAlignment(SwingConstants.CENTER);
        nickLabel.setBounds(294, 169, 140, 25);
        card.add(nickLabel);

        // --------------------------------------------------------------------------------------------------------------
        // 4. 닫기 버튼
        // --------------------------------------------------------------------------------------------------------------
        JButton closeBtn = UIUtils.createSecondaryButton("닫기");
        closeBtn.setBounds(180, 312, 120, 40);
        closeBtn.addActionListener(e -> {
            if (mainFrame != null) {
                mainFrame.hideProfilePopup();
            }
        });
        card.add(closeBtn);

        return card;
    }

    private void setupHoverAndEditEvents() {
        MouseAdapter photoAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                editHintLabel.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setCursor(Cursor.getDefaultCursor());
                editHintLabel.setVisible(false);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                chooseProfileImage();
            }
        };

        profileImgLabel.setToolTipText("프로필 사진 변경");
        editHintLabel.setToolTipText("프로필 사진 변경");
        profileImgLabel.addMouseListener(photoAdapter);
        editHintLabel.addMouseListener(photoAdapter);
        nickLabel.setToolTipText("닉네임 변경");
        nickLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        nickLabel.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openEditDialog(); }
        });
    }

    private void chooseProfileImage() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("프로필 사진 선택");
        chooser.setFileFilter(new FileNameExtensionFilter("이미지 파일 (PNG, JPG, GIF)", "png", "jpg", "jpeg", "gif"));
        if (chooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        try {
            BufferedImage source = ImageIO.read(chooser.getSelectedFile());
            if (source == null) throw new IllegalArgumentException("지원하는 이미지 파일이 아닙니다.");
            String encoded = ProfileImageCodec.encodeForUpload(source);
            NetworkManager.getInstance().requestProfilePic(encoded);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "프로필 사진을 읽거나 전송할 수 없습니다: " + ex.getMessage(),
                    "프로필 사진 변경 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void openEditDialog() {
        String newNick = JOptionPane.showInputDialog(
                this,
                "변경할 닉네임을 입력하세요:",
                "프로필 수정",
                JOptionPane.PLAIN_MESSAGE

        );

        if (newNick != null && !newNick.trim().isEmpty()) {
            String candidate = newNick.trim();
            if (!isValidNickname(candidate)) {
                JOptionPane.showMessageDialog(this,
                        "닉네임은 1~30자이며 쉼표, 구분자, 줄바꿈을 사용할 수 없습니다.",
                        "잘못된 닉네임", JOptionPane.WARNING_MESSAGE);
                return;
            }
            NetworkManager.getInstance().requestChangeNick(candidate);
        }
    }

    private void handleNicknameResponse(String packet) {
        if (packet == null) return;
        if (packet.startsWith("RES_PIC_PROFILE|")) {
            String[] imageTokens = packet.split("\\|", 3);
            if (imageTokens.length >= 2 && "SUCCESS".equals(imageTokens[1])) {
                refreshData();
            } else if (imageTokens.length >= 3) {
                JOptionPane.showMessageDialog(this, imageTokens[2], "프로필 사진 변경 실패", JOptionPane.WARNING_MESSAGE);
            }
            return;
        }
        if (!packet.startsWith("RES_CHANGE_NICK|")) return;
        String[] tokens = packet.split("\\|", -1);
        if (tokens.length >= 3 && "SUCCESS".equals(tokens[1])) {
            refreshData();
        } else if (tokens.length >= 3) {
            JOptionPane.showMessageDialog(this, tokens[2], "닉네임 변경 실패", JOptionPane.WARNING_MESSAGE);
        }
    }

    private static boolean isValidNickname(String value) {
        if (value == null || value.isEmpty() || value.length() > 30
                || value.indexOf('|') >= 0 || value.indexOf(',') >= 0) return false;
        for (int i = 0; i < value.length(); i++)
            if (Character.isISOControl(value.charAt(i))) return false;
        return true;
    }

    private ImageIcon loadProfileImage(String resourcePath, int width, int height) {
        try {
            java.net.URL imgURL = getClass().getResource(resourcePath);
            if (imgURL != null) {
                ImageIcon originalIcon = new ImageIcon(imgURL);
                Image squareImage = ProfileImageCodec.centerCropToSquare(originalIcon.getImage());
                Image scaledImage = squareImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (Exception ignored) {}
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
