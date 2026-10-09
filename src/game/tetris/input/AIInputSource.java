package game.tetris.input;

import game.tetris.flow.TetrisFlow;
import game.tetris.input.AI.TetrisAI;

import java.util.ArrayDeque;
import java.util.Queue;

class AIInputSource implements IInputSource
{
    private int moveIntervalFrames;
    private final int MIN_MOVE_INTERVAL_FRAMES = 2;

    private final TetrisFlow flow;
    private final TetrisAI ai = new TetrisAI();
    private final Queue<InputEnum> moveQueue = new ArrayDeque<>();

    private int waitFrames;

    public AIInputSource(TetrisFlow flow, int moveIntervalFrames)
    {
        this.flow = flow;
        if(moveIntervalFrames < MIN_MOVE_INTERVAL_FRAMES)
            this.moveIntervalFrames = MIN_MOVE_INTERVAL_FRAMES;
        else
            this.moveIntervalFrames = moveIntervalFrames;
    }

    @Override
    public InputData getInputData()
    {
        InputData result = new InputData();

        if (waitFrames > 0)
        {
            waitFrames--;
            return result;
        }

        if (moveQueue.isEmpty())
            requestNewMoveList();

        if (moveQueue.isEmpty())
            return result;

        result.set(moveQueue.poll(), true);
        waitFrames = moveIntervalFrames - 1;

        return result;
    }

    private void requestNewMoveList()
    {
        InputEnum[] moveList = ai.getMoveList(flow.getBoard(), flow.getCurrentPieceInfo(), flow.getHoldPiece() );

        if (moveList == null)
            return;

        for (InputEnum input : moveList)
            moveQueue.add(input);
    }
}