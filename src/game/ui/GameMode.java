package game.ui;

import game.tetris.input.InputSourceType;

// 게임 모드별 참가자 구성. 보드마다 입력 소스를 하나씩 가지므로
// UI는 모드 분기 없이 플레이어 수만큼 Tetris + GamePanel을 만들면 됨
public enum GameMode
{
    SINGLE("1인용", true, InputSourceType.Keyboard1),
    // InputTranslator가 AIInputSource에 null flow를 넘기는 문제가 해결될 때까지 비활성화
    VS_AI("AI 대전", false, InputSourceType.Keyboard1, InputSourceType.AI),
    // InputTranslator가 Keyboard2를 지원할 때까지 비활성화 (현재는 예외 발생)
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