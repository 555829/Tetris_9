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

/**
 * Keeps the top scores and persists them to a tab-separated text file,
 * so the ranking survives closing the game.
 */
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

    /** Stored in the user's home folder so the file is never committed to git by accident. */
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

    /** True if this score would make it onto the board, so the UI knows whether to ask for a name. */
    public boolean qualifies(int score)
    {
        return entries.size() < MAX_ENTRIES
                || score > entries.get(entries.size() - 1).getScore();
    }

    /**
     * Adds a result and saves the board.
     * @return 0-based rank of the new entry, or -1 if it didn't make the top list
     */
    public int add(String name, int score, int level, int lines)
    {
        if (!qualifies(score))
            return -1;

        RankingEntry entry = new RankingEntry(cleanName(name), score, level, lines);

        // Insert after any equal scores: on a tie, the earlier record keeps the higher rank.
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
            // A failed save should never crash the game; the ranking just won't persist.
            System.err.println("Could not save ranking file: " + e.getMessage());
        }
    }

    // A hand-edited or half-written file must not crash the game, so bad lines are skipped.
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

    // Tabs and line breaks would corrupt the file format.
    private static String cleanName(String name)
    {
        String cleaned = name == null ? "" : name.replaceAll("[\\t\\r\\n]", " ").trim();
        if (cleaned.isEmpty())
            return DEFAULT_NAME;
        return cleaned.length() > MAX_NAME_LENGTH ? cleaned.substring(0, MAX_NAME_LENGTH) : cleaned;
    }
}