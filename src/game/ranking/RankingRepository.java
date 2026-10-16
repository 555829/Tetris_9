package game.ranking;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// 상위 점수(TOP 10)를 보관하고 탭으로 구분한 텍스트 파일에 저장
public final class RankingRepository
{
    public static final int MAX_ENTRIES = 10;
    private static final int MAX_NAME_LENGTH = 12;
    private static final String DEFAULT_NAME = "PLAYER";

    private final Path file;
    private final List<RankingEntry> entries = new ArrayList<>();

    public RankingRepository(Path file)
    {
        this.file = file;
        load();
    }

    // 사용자 홈 폴더에 저장 → 프로젝트 폴더 밖이라 기록 파일이 git에 올라가지 않음
    public static RankingRepository openDefault()
    {
        return new RankingRepository(
                Paths.get(System.getProperty("user.home"), ".tetris9", "ranking.tsv"));
    }

    public List<RankingEntry> getTop()
    {
        return Collections.unmodifiableList(entries);
    }

    public int getBestScore()
    {
        return entries.isEmpty() ? 0 : entries.get(0).getScore();
    }

    // 이 점수가 TOP 10에 들어가는지 확인. 화면에서 이름 입력을 받을지 결정할 때 사용
    public boolean qualifies(int score)
    {
        return entries.size() < MAX_ENTRIES
                || score > entries.get(entries.size() - 1).getScore();
    }

    // 기록을 추가하고 파일에 저장
    // 반환값: 새 기록의 순위(0부터 시작), TOP 10에 못 들면 -1
    public int add(String name, int score, int level, int lines)
    {
        if (!qualifies(score))
            return -1;

        RankingEntry entry = new RankingEntry(cleanName(name), score, level, lines);

        // 같은 점수가 있으면 그 뒤에 넣음 → 동점이면 먼저 세운 기록이 위 순위
        int index = 0;
        while (index < entries.size() && entries.get(index).getScore() >= score)
            index++;
        entries.add(index, entry);

        while (entries.size() > MAX_ENTRIES)
            entries.remove(entries.size() - 1);

        save();
        return index;
    }

    private void load()
    {
        if (!Files.exists(file))
            return;

        try
        {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8))
            {
                RankingEntry entry = parse(line);
                if (entry != null)
                    entries.add(entry);
            }
        }
        catch (IOException e)
        {
            System.err.println("Could not read ranking file: " + e.getMessage());
        }

        entries.sort(Comparator.comparingInt(RankingEntry::getScore).reversed());
        while (entries.size() > MAX_ENTRIES)
            entries.remove(entries.size() - 1);
    }

    private void save()
    {
        List<String> lines = new ArrayList<>();
        for (RankingEntry e : entries)
            lines.add(e.getName() + "\t" + e.getScore() + "\t" + e.getLevel() + "\t" + e.getLines());

        try
        {
            Files.createDirectories(file.getParent());
            Files.write(file, lines, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            // 저장에 실패해도 게임은 멈추지 않음 (랭킹만 저장되지 않음)
            System.err.println("Could not save ranking file: " + e.getMessage());
        }
    }

    // 파일을 직접 고쳤거나 쓰다 만 경우에도 게임이 멈추지 않도록, 형식이 잘못된 줄은 건너뜀
    private static RankingEntry parse(String line)
    {
        String[] parts = line.split("\t");
        if (parts.length != 4)
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

    // 이름에 탭이나 줄바꿈이 있으면 파일 형식이 깨지므로 공백으로 바꾸고, 12자로 자름
    private static String cleanName(String name)
    {
        String cleaned = name == null ? "" : name.replaceAll("[\\t\\r\\n]", " ").trim();
        if (cleaned.isEmpty())
            return DEFAULT_NAME;
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }
}