package game.tetris.flow;

// 블록 모양 바꾸기: 떨어지던 블록을 다른 모양으로 바꿈
final class MorphEvent implements BlockEvent
{
    @Override
    public boolean apply(TetrisFlow flow)
    {
        flow.morph();
        return false;  // 모양만 바뀌고 낙하는 계속됨
    }
}