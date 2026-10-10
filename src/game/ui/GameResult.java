package game.ui;


// 게임이 끝났을 때 게임 화면이 결과 화면으로 넘겨주는 정보
public final class GameResult
{
    private final GameMode mode;
    private final int winner; // 승리한 플레이어 번호 (0 = 1P), 1인용이면 -1
    private final int score;
    private final int level;
    private final int lines;

    // 점수/레벨/줄은 항상 1P 기준. 랭킹에는 사람 플레이어 기록만 올라가기 때문
    public GameResult(GameMode mode, int winner, int score, int level, int lines)
    {
        this.mode = mode;
        this.winner = winner;
        this.score = score;
        this.level = level;
        this.lines = lines;
    }

    public GameMode getMode() { return mode; }

    public int getWinner() { return winner; }

    public int getScore() { return score; }

    public int getLevel() { return level; }

    public int getLines() { return lines; }
}