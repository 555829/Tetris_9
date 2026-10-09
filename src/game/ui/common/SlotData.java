package game.ui.common;

import java.awt.Color;

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