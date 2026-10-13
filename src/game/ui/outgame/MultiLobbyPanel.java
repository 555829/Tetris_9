package game.ui.outgame;

import game.NetworkManager;
import game.ui.MainFrame;
import game.ui.common.UIUtils;

import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
//======================================================================================================================
// 멀티플레이 로비
//======================================================================================================================
public class MultiLobbyPanel extends JPanel {
    private MainFrame mainFrame;
    private DefaultTableModel roomTableModel;
    private JTable roomTable;

    public MultiLobbyPanel(MainFrame mainFrame) {
        this.mainFrame = mainFrame;

        setLayout(new BorderLayout(15, 15));
        setBackground(UIUtils.COLOR_BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JPanel topPanel = new JPanel(new BorderLayout(18, 0));
        topPanel.setOpaque(false);

        JPanel leftGroup = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 0));
        leftGroup.setOpaque(false);
        JButton logoutBtn = UIUtils.createDangerButton("로그아웃");
        logoutBtn.setPreferredSize(new Dimension(78, 36));
        logoutBtn.setMaximumSize(new Dimension(78, 36));
        JLabel lobbyTitle = UIUtils.createLabel("멀티플레이", 27f, UIUtils.COLOR_TEXT);
        leftGroup.add(logoutBtn);
        leftGroup.add(lobbyTitle);

        JPanel btnGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        btnGroup.setOpaque(false);

        JButton createBtn = createLobbyButton("방 만들기");
        UIUtils.styleButton(createBtn, true);
        JButton refreshBtn = createLobbyButton("새로고침");
        JButton shopBtn = createLobbyButton("상점");
        JButton profileBtn = createLobbyButton("프로필");
        JButton settingsBtn = createLobbyButton("설정");

        btnGroup.add(createBtn);
        btnGroup.add(refreshBtn);
        btnGroup.add(shopBtn);
        btnGroup.add(profileBtn);
        btnGroup.add(settingsBtn);

        topPanel.add(leftGroup, BorderLayout.WEST);
        topPanel.add(btnGroup, BorderLayout.EAST);

        createBtn.addActionListener(e -> mainFrame.showRoomCreatePopup((roomTitle, mode) -> {
            NetworkManager.getInstance().createRoom(roomTitle, mode);
            mainFrame.hideRoomCreatePopup();
        }));

        refreshBtn.addActionListener(e -> NetworkManager.getInstance().requestRoomList());
        shopBtn.addActionListener(e -> mainFrame.showShopPopup());
        profileBtn.addActionListener(e -> mainFrame.showProfilePopup());
        settingsBtn.addActionListener(e -> mainFrame.showSettingsPopup());
        logoutBtn.addActionListener(e -> mainFrame.showLogoutPopup());

        String[] headers = { "방 제목", "방장", "게임 모드", "인원 수", "room_id" };
        roomTableModel = new DefaultTableModel(headers, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        roomTable = new JTable(roomTableModel);
        roomTable.removeColumn(roomTable.getColumnModel().getColumn(4));
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(440);
        roomTable.getColumnModel().getColumn(1).setPreferredWidth(230);
        roomTable.getColumnModel().getColumn(2).setPreferredWidth(220);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(130);
        roomTable.getTableHeader().setReorderingAllowed(false);
        roomTable.getTableHeader().setResizingAllowed(false);

        roomTable.setBackground(UIUtils.COLOR_SURFACE);
        roomTable.setForeground(UIUtils.COLOR_TEXT);
        roomTable.setSelectionBackground(new Color(57, 95, 116));
        roomTable.setSelectionForeground(Color.WHITE);
        roomTable.setRowHeight(44);
        roomTable.setFont(UIUtils.getBodyFont(Font.PLAIN, 14f));
        roomTable.setShowGrid(false);
        roomTable.setIntercellSpacing(new Dimension(0, 1));
        roomTable.setFillsViewportHeight(true);
        roomTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        roomTable.getTableHeader().setBackground(UIUtils.COLOR_SURFACE_RAISED);
        roomTable.getTableHeader().setForeground(UIUtils.COLOR_MUTED);
        roomTable.getTableHeader().setFont(UIUtils.getFont(Font.BOLD, 13f));
        roomTable.getTableHeader().setPreferredSize(new Dimension(0, 35));

        JScrollPane scrollPane = new JScrollPane(roomTable);
        scrollPane.getViewport().setBackground(UIUtils.COLOR_SURFACE);
        scrollPane.setBorder(new LineBorder(UIUtils.COLOR_BORDER, 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(18);
        JPanel tableSurface = new JPanel(new BorderLayout());
        tableSurface.setBackground(UIUtils.COLOR_SURFACE);
        tableSurface.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(UIUtils.COLOR_BORDER, 1), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        tableSurface.add(scrollPane, BorderLayout.CENTER);
        roomTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = roomTable.getSelectedRow();
                    if (row != -1) {
                        int modelRow = roomTable.convertRowIndexToModel(row);
                        Object roomId = roomTableModel.getValueAt(modelRow, 4);
                        try {
                            NetworkManager.getInstance().joinRoom(Integer.parseInt(String.valueOf(roomId)));
                        } catch (NumberFormatException ignored) {
                            // 선택된 행이 올바른 방 식별자를 갖지 않으면 입장 요청을 보내지 않습니다.
                        }
                    }
                }
            }
        });

        add(topPanel, BorderLayout.NORTH);
        add(tableSurface, BorderLayout.CENTER);

        setupNetworkListener();
    }

    private void setupNetworkListener() {
        NetworkManager.getInstance().addPacketListener(packet -> {
            if (packet == null || packet.trim().isEmpty()) return;
            String[] tokens = packet.split("\\|", -1);
            String cmd = tokens[0];

            if ("RES_ROOM_LIST".equals(cmd)) {
                SwingUtilities.invokeLater(() -> {
                    roomTableModel.setRowCount(0);
                    for (int i = 1; i < tokens.length; i++) {
                        if (tokens[i].trim().isEmpty()) continue;
                        String[] info = tokens[i].split(",", -1);
                        if (info.length >= 5) {
                            roomTableModel.addRow(new Object[]{
                                    info[1], info[2], info[3], info[4] + " / 4", info[0]
                            });
                        }
                    }
                });
            }
        });
    }

    private JButton createLobbyButton(String text) {
        JButton b = UIUtils.createSecondaryButton(text);
        b.setFont(UIUtils.getFont(Font.BOLD, 12f));
        b.setPreferredSize(new Dimension(94, 40));
        return b;
    }
}
