package game.ui.outgame.popup;

import Network.NetworkHandler;
import game.UserProfileState;
import game.ui.MainFrame;
import game.ui.common.UIUtils;
import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.util.EnumMap;
import java.util.Map;

//======================================================================================================================
// 상점 팝업을 띄워줌
//======================================================================================================================
public class ShopPopup extends JPanel {
    private int money;
    private MainFrame mainFrame;
    private JLabel userMoneyLabel;
    private final Map<CardSkin, JButton> actionButtons = new EnumMap<>(CardSkin.class);
    private boolean actionPending;
    private boolean awaitingActionRefresh;

    public void onBuyCard(String result)
    {
        actionPending = false;

        if ("SUCCESS".equals(result))
        {
            awaitingActionRefresh = true;
            refreshFromNetwork();
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "카드를 구매했습니다."));;
        }
        else
        {
            awaitingActionRefresh = false;
            refreshFromNetwork();
            SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, "카드를 구매했습니다."));;
        }
    }

    public void onEquipCard(String result)
    {
        actionPending = false;

        if ("SUCCESS".equals(result))
        {
            awaitingActionRefresh = true;
            refreshFromNetwork();
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "프로필 카드를 장착했습니다."));
        }
        else
        {
            awaitingActionRefresh = false;
            refreshFromNetwork();
            SwingUtilities.invokeLater(() ->
                    JOptionPane.showMessageDialog(this, "카드를 보유하고 있지 않습니다."));
        }
    }

    public void onUserInfo()
    {
        awaitingActionRefresh = false;
        refreshFromNetwork();
    }

    // 보통은 이렇게 구매가능한거는 sql에서 테이블 따로 빼놓고 보조키랑 지정해야하는데
    // 귀찮아서 한 준에 다 때려박아서 이렇게 짰습니다. 상점에 구매가능한 아이템도 4개밖에 없어서
    public enum CardSkin {
        WHITE("white", 1000, "/game/asset/card_white.png"),
        SILVER("silver", 3000, "/game/asset/card_silver.png"),
        GOLD("gold", 5000, "/game/asset/card_gold.png"),
        PLATINUM("platinum", 10000, "/game/asset/card_platinum.png");

        public final String name;
        public final int price;
        public final String imagePath;

        CardSkin(String name, int price, String imagePath) {
            this.name = name;
            this.price = price;
            this.imagePath = imagePath;
        }
    }

    //여기서부터 진짜 팝업 시작
    public ShopPopup(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout());
        setOpaque(false);

        // 상점 팝업 전체 박스 (너비 520, 높이 460)
        JPanel popupBox = UIUtils.createBasePopupBox(520, 460);
        popupBox.setLayout(new BorderLayout(0, 15));
        popupBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UIUtils.COLOR_BORDER),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)));

        // 카드 상점이랑 돈 써져있는 상단부
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel titleLabel = UIUtils.createLabel("카드 상점", 22f, Color.WHITE);
        userMoneyLabel = UIUtils.createLabel("0원", 15f, Color.YELLOW);
        topPanel.add(titleLabel, BorderLayout.WEST);
        topPanel.add(userMoneyLabel, BorderLayout.EAST);
        popupBox.add(topPanel, BorderLayout.NORTH);

        // 중앙 2x2 아이템 그리드
        JPanel gridPanel = new JPanel(new GridLayout(2, 2, 15, 15));
        gridPanel.setOpaque(false);

        for (CardSkin skin : CardSkin.values()) {
            gridPanel.add(createItemCard(skin));
        }
        popupBox.add(gridPanel, BorderLayout.CENTER);

        // 하단 닫기 버튼
        JButton closeBtn = UIUtils.createSecondaryButton("닫기");
        closeBtn.setPreferredSize(new Dimension(120, 38));
        closeBtn.addActionListener(e -> mainFrame.hideShopPopup());

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btnPanel.setOpaque(false);
        btnPanel.add(closeBtn);
        popupBox.add(btnPanel, BorderLayout.SOUTH);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0;
        gbc.weightx = 1.0; gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(popupBox, gbc);

        refreshFromNetwork();
    }


    //구매했으니까 상점 ui 바꿔주는 코드
    private void refreshFromNetwork()
    {
        UserProfileState profile = mainFrame.getUserProfile();

        if (userMoneyLabel != null)
            userMoneyLabel.setText(profile.getMoney() + "원");

        for (CardSkin skin : CardSkin.values())
        {
            JButton button = actionButtons.get(skin);

            if (button == null)
                continue;

            boolean owned = isOwned(profile, skin);
            boolean equipped =
                    skin.name().equalsIgnoreCase(profile.getEquippedSkin());

            button.setText(
                    equipped ? "장착 중" : owned ? "장착하기" : "구매하기");

            button.setEnabled(
                    !equipped && !actionPending && !awaitingActionRefresh);
        }
    }

    //카드를 가지고 있으면 변경으로 바꿔주는 코드
    private boolean isOwned(UserProfileState profile, CardSkin skin)
    {
        switch (skin)
        {
            case WHITE: return profile.hasWhiteCard();
            case SILVER: return profile.hasSilverCard();
            case GOLD: return profile.hasGoldCard();
            case PLATINUM: return profile.hasPlatinumCard();
            default: return false;
        }
    }

    // 아이템 단일 카드 UI
    private JPanel createItemCard(CardSkin skin) {
        JPanel card = new JPanel(new BorderLayout(5, 5));
        card.setBackground(new Color(40, 44, 58));
        card.setBorder(new LineBorder(new Color(255, 255, 255, 40), 1, true));

        // 🌟 1. 카드 미리보기 이미지 레이블 생성
        JLabel imgLabel = new JLabel();
        imgLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imgLabel.setPreferredSize(new Dimension(100, 100));

        // 🌟 2. 스킨에 맞는 이미지 경로 지정 (예: /game/asset/profile_card_bg_silver.png)
        String imgPath = "/game/asset/card_" + skin.name.toLowerCase() + ".png";
        java.net.URL imgURL = getClass().getResource(imgPath);

        if (imgURL != null) {
            // 이미지가 존재하면 100x100 크기로 스케일링하여 적용
            ImageIcon originalIcon = new ImageIcon(imgURL);
            Image scaledImage = originalIcon.getImage().getScaledInstance(230, 120, Image.SCALE_SMOOTH);
            imgLabel.setIcon(new ImageIcon(scaledImage));
        } else {
            // 이미지 경로를 찾지 못한 경우 기본 텍스트 표시 (예외 방어)
            imgLabel.setText(skin.name);
            imgLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
            imgLabel.setForeground(Color.LIGHT_GRAY);
        }

        // 하단 정보 및 구매/장착 버튼
        JPanel bottomInfo = new JPanel(new GridLayout(2, 1, 2, 2));
        bottomInfo.setOpaque(false);

        JLabel priceLabel = UIUtils.createLabel(skin.price + " G", 13f, Color.CYAN);
        priceLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JButton actionBtn = UIUtils.createStyledButton("구매하기");
        actionBtn.setPreferredSize(new Dimension(115, 34));
        actionBtn.addActionListener(e -> handleItemAction(skin, actionBtn));
        actionButtons.put(skin, actionBtn);

        bottomInfo.add(priceLabel);
        bottomInfo.add(actionBtn);

        card.add(imgLabel, BorderLayout.CENTER);
        card.add(bottomInfo, BorderLayout.SOUTH);

        return card;
    }


    private void handleItemAction(CardSkin skin, JButton btn)
    {
        if (!mainFrame.getNetworkConnection().isConnected())
        {
            JOptionPane.showMessageDialog(this, "서버에 연결한 뒤 이용할 수 있습니다.");
            return;
        }

        if (actionPending)
            return;

        actionPending = true;
        refreshFromNetwork();

        if (isOwned(mainFrame.getUserProfile(), skin))
            NetworkHandler.getInstance().requestEquipCard(skin.name());
        else
            NetworkHandler.getInstance().requestBuyCard(skin.name());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}
