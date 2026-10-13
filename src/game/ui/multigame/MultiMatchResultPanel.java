package game.ui.multigame;

import game.ui.common.UIUtils;
import game.profile.ProfileImageCodec;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javax.imageio.ImageIO;

//======================================================================================================================
// 멀티플레이 결과창 패널
//======================================================================================================================
public class MultiMatchResultPanel extends JPanel {
    private static final Color BACKGROUND = new Color(23, 26, 35);
    private static final Color PANEL = new Color(39, 44, 57);
    private static final Color TEXT = new Color(238, 241, 248);
    private static final Color MUTED = new Color(165, 174, 192);

    private final JLabel title = new JLabel("경기 결과", SwingConstants.CENTER);
    private final JLabel summary = new JLabel("", SwingConstants.CENTER);
    private final JPanel resultRows = new JPanel();

    public MultiMatchResultPanel(Runnable returnToRoom, Runnable returnToLobby) {
        setLayout(new GridBagLayout());
        setBackground(BACKGROUND);
        setBorder(new EmptyBorder(18, 24, 18, 24));

        title.setForeground(TEXT);
        title.setFont(UIUtils.getFont(Font.BOLD, 30f));
        summary.setForeground(new Color(255, 211, 112));
        summary.setFont(UIUtils.getFont(Font.BOLD, 18f));

        JPanel heading = new JPanel(new GridLayout(2, 1, 0, 4));
        heading.setOpaque(false);
        heading.setAlignmentX(Component.CENTER_ALIGNMENT);
        heading.add(title);
        heading.add(summary);

        resultRows.setLayout(new BoxLayout(resultRows, BoxLayout.Y_AXIS));
        resultRows.setBackground(BACKGROUND);
        resultRows.setOpaque(false);
        resultRows.setAlignmentX(Component.CENTER_ALIGNMENT);
        resultRows.setMaximumSize(new Dimension(760, 390));

        JButton roomButton = UIUtils.createStyledButton("방으로 돌아가기");
        roomButton.addActionListener(e -> returnToRoom.run());
        JButton lobbyButton = UIUtils.createSecondaryButton("로비로 나가기");
        lobbyButton.addActionListener(e -> returnToLobby.run());
        roomButton.setPreferredSize(new Dimension(170, 44));
        lobbyButton.setPreferredSize(new Dimension(170, 44));
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 0));
        actions.setOpaque(false);
        actions.setAlignmentX(Component.CENTER_ALIGNMENT);
        actions.add(roomButton);
        actions.add(lobbyButton);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.setMaximumSize(new Dimension(800, 560));
        content.add(heading);
        content.add(Box.createVerticalStrut(18));
        content.add(resultRows);
        content.add(Box.createVerticalStrut(14));
        content.add(actions);

        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.CENTER;
        constraints.fill = GridBagConstraints.NONE;
        add(content, constraints);
    }

    public void showResultPacket(String packet) {
        resultRows.removeAll();
        if (packet == null) {
            summary.setText("결과 정보를 읽을 수 없습니다.");
            return;
        }
        String[] fields = packet.split("\\|", -1);
        if (fields.length < 7 || !"RES_MATCH_RESULT".equals(fields[0])) {
            summary.setText("결과 정보를 읽을 수 없습니다.");
            return;
        }

        int winnerSlot = parseInt(fields[1], -1);
        title.setText("결과");
        if (winnerSlot < 0) summary.setText("무승부");
        else summary.setText((winnerSlot + 1) + "P 우승");

        List<ResultRecord> records = new ArrayList<>();
        for (int i = 7; i < fields.length; i++) {
            String[] record = fields[i].split(",", -1);
            if (record.length < 6 || !("HUMAN".equals(record[0]) || "BOT".equals(record[0]))) continue;
            records.add(new ResultRecord(record[0], record[1], parseInt(record[2], 0),
                    parseInt(record[3], 0), parseInt(record[4], 0), record.length >= 7 ? record[6] : ""));
        }
        records.sort(Comparator.comparingInt(record -> record.place <= 0 ? Integer.MAX_VALUE : record.place));
        for (ResultRecord record : records) {
            resultRows.add(createResultRow(record));
            if (record != records.get(records.size() - 1)) resultRows.add(Box.createVerticalStrut(8));
        }
        resultRows.revalidate();
        resultRows.repaint();
    }

    private JPanel createResultRow(ResultRecord record) {
        JPanel row = new JPanel(new BorderLayout(18, 0));
        row.setMaximumSize(new Dimension(760, 84));
        row.setPreferredSize(new Dimension(640, 84));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        row.setBackground(PANEL);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(rankColor(record.place), record.place == 1 ? 2 : 1),
                new EmptyBorder(8, 14, 8, 14)));

        JLabel place = new JLabel(record.place > 0 ? record.place + "위" : "-", SwingConstants.CENTER);
        place.setPreferredSize(new Dimension(68, 48));
        place.setForeground(rankColor(record.place));
        place.setFont(UIUtils.getFont(Font.BOLD, record.place == 1 ? 23f : 19f));

        JPanel identity = new JPanel(new BorderLayout(12, 0));
        identity.setOpaque(false);
        JLabel avatar = new AvatarLabel(record.nickname, record.kind, record.avatarData);
        identity.add(avatar, BorderLayout.WEST);
        JPanel nameStack = new JPanel(new GridLayout(2, 1, 0, 3));
        nameStack.setOpaque(false);
        JLabel nickname = new JLabel(record.nickname.isEmpty() ? "플레이어" : record.nickname);
        nickname.setForeground(TEXT);
        nickname.setFont(UIUtils.getFont(Font.BOLD, 17f));
        JLabel kind = new JLabel("BOT".equals(record.kind) ? "AI 플레이어" : "플레이어");
        kind.setForeground(MUTED);
        kind.setFont(UIUtils.getFont(Font.PLAIN, 12f));
        nameStack.add(nickname);
        nameStack.add(kind);
        identity.add(nameStack, BorderLayout.CENTER);

        JPanel stats = new JPanel(new GridLayout(1, 2, 18, 0));
        stats.setOpaque(false);
        stats.add(stat("점수", String.format("%,d", record.score)));
        stats.add(stat("클리어", record.lines + "줄"));

        row.add(place, BorderLayout.WEST);
        row.add(identity, BorderLayout.CENTER);
        row.add(stats, BorderLayout.EAST);
        return row;
    }

    private JPanel stat(String label, String value) {
        JPanel panel = new JPanel(new GridLayout(2, 1, 0, 2));
        panel.setOpaque(false);
        JLabel labelView = new JLabel(label, SwingConstants.RIGHT);
        labelView.setForeground(MUTED);
        labelView.setFont(UIUtils.getFont(Font.PLAIN, 11f));
        JLabel valueView = new JLabel(value, SwingConstants.RIGHT);
        valueView.setForeground(TEXT);
        valueView.setFont(UIUtils.getFont(Font.BOLD, 14f));
        panel.add(labelView);
        panel.add(valueView);
        return panel;
    }

    private static Color rankColor(int place) {
        if (place == 1) return new Color(255, 207, 82);
        if (place == 2) return new Color(195, 211, 232);
        if (place == 3) return new Color(215, 151, 105);
        return new Color(110, 122, 144);
    }

    private static int parseInt(String value, int fallback) {
        try { return Integer.parseInt(value); }
        catch (NumberFormatException ignored) { return fallback; }
    }

    private static final class ResultRecord {
        private final String kind;
        private final String nickname;
        private final int score;
        private final int lines;
        private final int place;
        private final String avatarData;

        private ResultRecord(String kind, String nickname, int score, int lines, int place, String avatarData) {
            this.kind = kind;
            this.nickname = nickname;
            this.score = score;
            this.lines = lines;
            this.place = place;
            this.avatarData = avatarData;
        }
    }

    /** 서버에서 사진을 보내지 않는 참가자에게 닉네임 첫 글자를 아바타처럼 그립니다. */
    private static final class AvatarLabel extends JLabel {
        private final Color color;
        private final BufferedImage avatar;

        private AvatarLabel(String nickname, String kind, String avatarData) {
            super(firstCharacter(nickname), SwingConstants.CENTER);
            color = "BOT".equals(kind) ? new Color(64, 118, 160) : new Color(80, 91, 125);
            avatar = decodeAvatar(avatarData);
            setPreferredSize(new Dimension(56, 56));
            setMinimumSize(new Dimension(56, 56));
            setOpaque(false);
            setForeground(Color.WHITE);
            setFont(UIUtils.getFont(Font.BOLD, 21f));
            if (avatar != null) setText("");
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (avatar != null) {
                g.drawImage(avatar, 1, 1, getWidth() - 2, getHeight() - 2, null);
            } else {
                g.setColor(color);
                g.fillRect(1, 1, getWidth() - 2, getHeight() - 2);
            }
            g.dispose();
        }

        private static String firstCharacter(String value) {
            return value == null || value.isEmpty() ? "?" : value.substring(0, 1).toUpperCase();
        }

        private static BufferedImage decodeAvatar(String encoded) {
            if (encoded == null || encoded.isEmpty()) return null;
            try {
                byte[] bytes = ProfileImageCodec.decodeFromServer(encoded);
                BufferedImage decoded = ImageIO.read(new ByteArrayInputStream(bytes));
                return decoded == null ? null : ProfileImageCodec.centerCropToSquare(decoded);
            } catch (Exception ignored) {
                return null;
            }
        }
    }
}
