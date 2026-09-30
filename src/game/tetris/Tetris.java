package game.tetris;

import game.tetris.flow.TetrisFlow;
import game.tetris.input.InputHandler;
import game.tetris.input.InputTranslator;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.board.*;
import game.tetris.timer.*;

// 클래스 바깥, Tetris를 필드로 가지는 상위 클래스에서 일정 시간 당 tick()을 해줘야 함.
// UI를 위한 Render의 원본 데이터 source 담당하는 메소드도 여기서 밑에 클래스들 모아서 만들기
public class Tetris {

    // 게임의 논리 시간(프레임) 관리
    private final timerClass timer = new timerClass();

    // 외부에서 게임의 시간 정보를 읽기 위한 인터페이스
    public final timerReader Timer = timer;

    // 실제 또는 가상 입력을 InputKey로 변환하고 입력 상태를 관리
    public final InputTranslator input;

    // 블록 생성 방식에 따라 다음 테트로미노들을 공급 및 보관
    public final TetrominoQueue queue;

    // 고정된 블록의 배치, 충돌 검사 및 라인 제거 담당
    public final Board board;

    // 현재 조작 중인 블록의 위치, 이동 및 회전 담당
    public final ActivePiece activePiece;

    // 입력을 게임 내 동작으로 변환. DAS/ARR 등의 조작 규칙 처리
    public final InputHandler inputHandler;

    // 게임 진행 규칙 관리. 중력, HOLD, 점수, 게임 상태 등 처리
    public final TetrisFlow flow;

    public Tetris(InputTranslator input, TetrominoQueue queue)
    {
        this.input = input;
        this.queue = queue;

        board = new Board(10, 20);
        activePiece = new ActivePiece(board);
        flow = new TetrisFlow(board, activePiece, queue, timer);
        inputHandler = new InputHandler(input, flow);

        flow.start();
    }
    public void tick()
    {
        input.tick();

        if (inputHandler.tickSystem())
            return;

        if (!flow.isPlaying())
            return;

        timer.tick();

        if (inputHandler.tickGameplay())
            return;

        if (flow.tick())
            inputHandler.reset();
    }
}