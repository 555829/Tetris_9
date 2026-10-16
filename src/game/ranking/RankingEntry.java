package game.ranking;

// 랭킹에 올라가는 게임 기록 1개 (이름, 점수, 레벨, 줄 수)
// 만든 뒤에는 값이 바뀌지 않음
public final class RankingEntry
{
    private final String name;
    private final int score;
    private final int level;
    private final int lines;

    public RankingEntry(String name, int score, int level, int lines)
    {
        this.name = name;
        this.score = score;
        this.level = level;
        this.lines = lines;
    }

    public String getName() { return name; }

    public int getScore() { return score; }

    public int getLevel() { return level; }

    public int getLines() { return lines; }
}