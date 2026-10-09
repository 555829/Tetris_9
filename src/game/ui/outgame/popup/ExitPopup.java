package game.ui.outgame.popup;

import game.ui.MainFrame;
import game.ui.common.UIUtils;

import javax.swing.*;
import java.awt.*;

public class ExitPopup extends JPanel {
    private MainFrame mainFrame; //메인프레임 가져오기
    private JPanel popupBox;    //팝업박스 생성

    public ExitPopup(MainFrame mainFrame) {
        //필요한 설정 모아둠과 동시에 gridbagcontains가 popupbox를 화면중앙으로 잡아줌
        this.mainFrame = mainFrame;
        setLayout(new GridBagLayout()); //중앙 정렬
        setOpaque(false);
        popupBox = UIUtils.createBasePopupBox(400, 200); //팝업 박스 사이즈 설정
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        add(popupBox, gbc);

        //버튼 생성
        JLabel titleLabel = UIUtils.createLabel("정말 종료하시겠습니까?", 18f, Color.WHITE);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        btnPanel.setOpaque(false);
        JButton yesBtn = UIUtils.createStyledButton("예");
        JButton noBtn = UIUtils.createStyledButton("아니오");

        //버튼 크기조절
        Dimension popupBtnSize = new Dimension(120, 45);
        yesBtn.setPreferredSize(popupBtnSize);
        yesBtn.setMaximumSize(popupBtnSize);
        noBtn.setPreferredSize(popupBtnSize);
        noBtn.setMaximumSize(popupBtnSize);

        //버튼 역할 부여
        yesBtn.addActionListener(e -> System.exit(0));
        noBtn.addActionListener(e -> mainFrame.hideExitPopup());

        //버튼 패널에 추가
        btnPanel.add(yesBtn);
        btnPanel.add(noBtn);

        //버튼 레이아웃
        popupBox.add(Box.createVerticalStrut(20)); //버튼 사이의 여백이 담김
        popupBox.add(titleLabel);   //정말 게임을 종료하시겠습니까가 담김
        popupBox.add(Box.createVerticalStrut(40)); //버튼 사이의 여백이 담김
        popupBox.add(btnPanel); //버튼들 모여있는 패널임
    }

    @Override //팝업 띄웠을때 뒤에 반투명 배경효과 주는코드
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.setColor(new Color(0, 0, 0, 170));
        g.fillRect(0, 0, getWidth(), getHeight());
    }
}