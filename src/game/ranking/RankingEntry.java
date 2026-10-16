package game.ranking;

/** One finished game on the ranking board. Immutable so the board can hand entries out safely. */
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