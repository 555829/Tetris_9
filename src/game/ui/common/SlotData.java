package game.ui.common;

import java.awt.Color;
//======================================================================================================================
//멀티플레이시 슬롯 정보 전달 (닉네임 , 봇 , 슬롯이 닫혀있는지 , 열려있는지 , 비어있는지 )
//======================================================================================================================
public class SlotData {
    public String name;
    public String type;  // PLAYER, BOT_EASY, BOT_HARD, CLOSED, OPEN, EMPTY
    public Color color;
    public String icon;

    public SlotData(String name, String type, Color color, String icon) {
        this.name = name;
        this.type = type;
        this.color = color;
        this.icon = icon;
    }
}