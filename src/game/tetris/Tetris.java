package game.tetris;

import game.tetris.flow.TetrisFlow;
import game.tetris.input.InputHandler;
import game.tetris.input.InputTranslator;
import game.tetris.render.TetrisTextRenderer;
import game.tetris.tetrominoQueue.TetrominoQueue;
import game.tetris.board.*;
import game.tetris.timer.*;

// 클래스 바깥, Tetris를 필드로 가지는 상위 클래스에서 일정 시간 당 tick()을 해줘야 함.
//
public class Tetris {

    private final timerClass timer = new timerClass();

    public final timerReader Timer = timer;
    // 실제 또는 가상 입력을 InputKey로 변환하는 역할
    public final InputTranslator input;
    public final TetrominoQueue queue;
    public final Board board;
    public final ActivePiece activePiece;
    public final InputHandler inputHandler;
    public final TetrisFlow flow;
    private final TetrisTextRenderer textRenderer;

    public Tetris(InputTranslator input, TetrominoQueue queue)
    {
        this.input = input;
        this.queue = queue;

        board = new Board(10, 20);
        activePiece = new ActivePiece(board);
        flow = new TetrisFlow(board, activePiece, queue, timer);
        inputHandler = new InputHandler(input, flow);
        textRenderer = new TetrisTextRenderer(
                board,
                activePiece,
                queue,
                flow
        );

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

    public String renderText() {
        return textRenderer.render();
    }
}