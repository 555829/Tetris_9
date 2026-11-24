package game.ranking;

// 랭킹에 올라가는 게임 기록 1개 (이름, 점수, 레벨, 줄 수)
// 만든 뒤에는 값이 바뀌지 않음
public final class RankingEntry
{
    // 파일 한 줄 형식: 이름<탭>점수<탭>레벨<탭>줄 수
    private static final String SEPARATOR = "\t";
    private static final int FIELD_COUNT = 4;

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

    // 파일에 저장할 한 줄로 바꿈
    String toLine()
    {
        return String.join(SEPARATOR, name,
                String.valueOf(score), String.valueOf(level), String.valueOf(lines));
    }

    // 파일의 한 줄을 기록으로 바꿈
    // 파일을 직접 고쳤거나 쓰다 만 경우에도 게임이 멈추지 않도록, 형식이 잘못된 줄은 null을 돌려줌
    static RankingEntry fromLine(String line)
    {
        String[] parts = line.split(SEPARATOR);
        if (parts.length != FIELD_COUNT)
            return null;

        try
        {
            return new RankingEntry(
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]));
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }
}