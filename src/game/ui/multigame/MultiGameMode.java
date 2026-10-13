package game.ui.multigame;

public enum MultiGameMode {
    TIME_ATTACK("시간전 (90초)", true, 90, 0, false),
    CLASSIC_40("클래식 (40줄)", true, 0, 40, false),
    ITEM_MATCH("아이템전", true, 0, 0, true);

    private final String label;
    private final boolean available;
    private final int timeLimitSec; // 제한 시간(초) - 0이면 무제한
    private final int targetLines;   // 목표 완료 줄 수 - 0이면 무제한
    private final boolean itemEnabled; // 아이템 블록 생성 여부

    MultiGameMode(String label, boolean available, int timeLimitSec, int targetLines, boolean itemEnabled) {
        this.label = label;
        this.available = available;
        this.timeLimitSec = timeLimitSec;
        this.targetLines = targetLines;
        this.itemEnabled = itemEnabled;
    }

    public String getLabel() {
        return label;
    }

    public boolean isAvailable() {
        return available;
    }

    public int getTimeLimitSec() {
        return timeLimitSec;
    }

    public int getTargetLines() {
        return targetLines;
    }

    public boolean isItemEnabled() {
        return itemEnabled;
    }

    public static MultiGameMode fromLabel(String labelStr) {
        if (labelStr != null) {
            for (MultiGameMode mode : values()) {
                if (mode.getLabel().contains(labelStr) || labelStr.contains(mode.getLabel())) {
                    return mode;
                }
            }
        }
        return TIME_ATTACK; // 기본 모드
    }
}
