package game.tetris.flow;

// 블록 차기: 떨어지던 블록을 차서 날려 보냄
final class KickEvent implements BlockEvent
{
    @Override
    public boolean apply(TetrisFlow flow)
    {
        flow.kick();
        return true;   // 블록이 날아가므로 이번 프레임의 낙하는 건너뜀
    }
}