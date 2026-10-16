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
    private int seenMorphCount;

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

        // 블록이 차여서 날아가는 중이면 조작할 블록이 없음 (getCurrentPieceInfo()가 null)
        // 이전 블록용 이동 목록이 다음 블록에 적용되지 않도록 비워 둠
        if (flow.getCurrentPieceInfo() == null)
        {
            moveQueue.clear();
            return result;
        }

        // 블록 모양이 바뀌면 예전 모양 기준으로 세운 이동 목록은 버리고 새로 계산
        // (홀드로 블록이 바뀌는 경우와 구분하려고 모양 바뀐 횟수로 판단)
        if (flow.getMorphCount() != seenMorphCount)
        {
            moveQueue.clear();
            seenMorphCount = flow.getMorphCount();
        }



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