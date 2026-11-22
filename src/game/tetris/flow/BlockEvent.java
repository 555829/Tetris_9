package game.tetris.flow;

// 블록이 떨어지는 도중 일어나는 방해 이벤트 (차기, 모양 바꾸기)
// 새 이벤트를 추가할 때 TetrisFlow는 고치지 않고 이 인터페이스를 구현한 클래스만 추가하면 됨
interface BlockEvent
{
    // 이벤트 실행. true를 돌려주면 이번 프레임의 일반 낙하를 건너뜀
    boolean apply(TetrisFlow flow);
}