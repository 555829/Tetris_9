package game.tetris.timer;

public class timerClass implements timerReader
{
    private int frameCount = 0;
    public int getFrameCount() { return frameCount; }
    public void tick(){ frameCount++; }
    public void reset() {frameCount = 0;}
    public boolean IsOver(int target){ return frameCount >= target;}
    public boolean IsUnder(int target){return frameCount < target;}
    public boolean IsBetween(int min, int max)
    { return frameCount >= min && frameCount < max; }
}
