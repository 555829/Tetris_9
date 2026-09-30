package game.tetris.timer;

public interface timerReader {
    public int getFrameCount();
    public boolean IsOver(int target);
    public boolean IsUnder(int target);
    public boolean IsBetween(int min, int max);
}
