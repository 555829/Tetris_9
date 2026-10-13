package game.ui;

import game.tetris.input.InputSourceType;

// 게임 모드별 참가자 구성. 보드마다 입력 소스를 하나씩 가지므로
// UI는 모드 분기 없이 플레이어 수만큼 Tetris + GamePanel을 만들면 됨
public enum GameMode
{
    SINGLE("1인용", true, InputSourceType.Keyboard1),
    VS_AI("AI 대전", true, InputSourceType.Keyboard1, InputSourceType.AI),
    // Keyboard2는 원격 보드 표시 전용이며, 2P 로컬 키 매핑은 아직 정의되어 있지 않음
    TWO_PLAYER("2인용", false, InputSourceType.Keyboard1, InputSourceType.Keyboard2);

    private final String label;
    private final boolean available;
    private final InputSourceType[] players;

    GameMode(String label, boolean available, InputSourceType... players)
    {
        this.label = label;
        this.available = available;
        this.players = players;
    }

    public String getLabel() { return label; }

    public boolean isAvailable() { return available; }

    public InputSourceType[] getPlayers() { return players.clone(); }

    public boolean isVersus() { return players.length > 1; }
}