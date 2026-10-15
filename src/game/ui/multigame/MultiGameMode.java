package game.ui.multigame;

public enum MultiGameMode {
    TIME_ATTACK("시간전 (90초)", 90, 0),
    CLASSIC_40("클래식 (40줄)", 0, 40);

    private final String label;
    private final int timeLimitSec; // 제한 시간(초) - 0이면 무제한
    private final int targetLines;   // 목표 완료 줄 수 - 0이면 무제한

    MultiGameMode(String label, int timeLimitSec, int targetLines) {
        this.label = label;
        this.timeLimitSec = timeLimitSec;
        this.targetLines = targetLines;
    }

    public String getLabel() {
        return label;
    }

    public int getTimeLimitSec() {
        return timeLimitSec;
    }

    public int getTargetLines() {
        return targetLines;
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
