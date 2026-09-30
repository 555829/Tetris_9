package game.tetris.input;

import game.tetris.flow.TetrisFlow;

public class InputHandler {
    private final InputTranslator input;
    private final TetrisFlow gameFlow;
    // 이동 키를 길게 누른 것으로 판정할 때까지 키를 눌러야 하는 시간
    private int DAS = 10;
    //  이동 키를 길게 눌렀을 때 블록이 다음 칸으로 이동할 때까지 걸리는 시간
    private int ARR = 3;

    private int horizontalFrames;
    private int heldDirection;


    // 소프트 드롭 관련
    private static final int SOFT_DROP_INTERVAL = 2; //TODO: 사람이 바이브 보면서 조정
    private int softDropFrames;

    public InputHandler(InputTranslator input, TetrisFlow gameFlow)
    {
        this.input = input;
        this.gameFlow = gameFlow;
    }

    // 정지 상태에서도 처리해야 하는 입력
    // true : 입력을 받고 게임 상태가 변경되었으니 이번 프레임 tick 취소
    public boolean tickSystem()
    {
        if(input.getKeyDown(InputEnum.Menu))
        {
            gameFlow.openMenu();
            reset();
            return true;
        }
        if (input.getKeyDown(InputEnum.Pause)
                && !gameFlow.isGameOver())
        {
            gameFlow.togglePause();
            reset();
            return true;
        }

        return false;
    }


    // 게임 플레이 중 사용하는 입력
    // true : 입력을 받고 블록 상태가 변경되었으니 이번 프레임 tick 취소
    public boolean tickGameplay()
    {
        //Hold와 DropDown은 블록을 바꾸므로, true를 반환해서 다른 Tick을 멈춤
        if (input.getKeyDown(InputEnum.Hold))
        {
            if (gameFlow.hold())
            {
                reset();
                return true;
            }
        }

        if (input.getKeyDown(InputEnum.DropDown))
        {
            if (gameFlow.hardDrop())
            {
                reset();
                return true;
            }
        }
        handleHorizontal();
        handleRotation();
        handleSoftDrop();
        return false;
    }

    // 입력 반복 상태 초기화 (키 누른 시간 초기화)
    public void reset()
    {
        horizontalFrames = 0;
        heldDirection = 0;
        softDropFrames = 0;
    }

    private void handleHorizontal()
    {
        boolean left = input.getKey(InputEnum.Left);
        boolean right = input.getKey(InputEnum.Right);

        int direction = 0;

        if (left && !right)
            direction = -1;
        else if (right && !left)
            direction = 1;

        // 양쪽을 동시에 누르거나 모두 떼면 정지
        if (direction == 0)
        {
            heldDirection = 0;
            horizontalFrames = 0;
            return;
        }

        // 새 방향 입력이면 즉시 한 칸 이동
        if (direction != heldDirection)
        {
            gameFlow.moveHorizontal(direction);

            heldDirection = direction;
            horizontalFrames = 0;
            return;
        }

        // 유지 입력: DAS 이후 ARR 간격으로 반복
        horizontalFrames++;

        if (horizontalFrames < DAS)
            return;

        //ARR이 0이면 즉시 끝까지 이동
        if (ARR == 0)
        {
            if (horizontalFrames == DAS)
            {
                while (gameFlow.moveHorizontal(direction))
                {}
            }
            return;
        }
        if ((horizontalFrames - DAS) % ARR == 0)
        {
            gameFlow.moveHorizontal(direction);
        }
    }
    private void handleRotation()
    {
        if(input.getKeyDown(InputEnum.Rotate))
        {
            gameFlow.rotateRight();
        }
    }
    private void handleSoftDrop()
    {
        if (!input.getKey(InputEnum.OneLineDown))
        {
            softDropFrames = 0;
            return;
        }
        // 처음 누른 순간 즉시 한 칸
        if (input.getKeyDown(InputEnum.OneLineDown))
        {
            gameFlow.softDrop();
            softDropFrames = 0;
            return;
        }

        // 계속 누르고 있으면 일정 간격으로 낙하
        softDropFrames++;

        if (softDropFrames >= SOFT_DROP_INTERVAL)
        {
            gameFlow.softDrop();
            softDropFrames = 0;
        }

    }
}
