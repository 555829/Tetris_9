package game.tetris.input;

import game.tetris.flow.TetrisFlow;

public class InputTranslator {
    private final IInputSource inputSource;
    private TetrisFlow flow;
    private InputData curInputData = new InputData();
    private InputData preInputData = new InputData();

    public InputTranslator(InputSourceType type, TetrisFlow flow)
    {
        inputSource = createInputSource(type);
        this.flow = flow;
    }

    private IInputSource createInputSource(InputSourceType type) {
        switch (type)
        {
            case InputSourceType.Keyboard1 :
                return new KeyboardInputSource(); //TODO : keyboard2 추가
            case InputSourceType.AI:
                return new AIInputSource(flow, 5); // TODO: 설정 파일 읽는 것으로 변경
            default:
                throw new IllegalArgumentException("Invalid InputSourceType");
        }
    }



    /*
    original input :  000111000
    getKey         :  000111000
    getKeyDown     :  000100000
    getKeyUp       :  000000100
     */
    public boolean getKey(InputEnum input) {
        return curInputData.get(input);
    }

    public boolean getKeyDown(InputEnum input) {
        return curInputData.get(input)
                && !preInputData.get(input);
    }

    public boolean getKeyUp(InputEnum input) {
        return !curInputData.get(input)
                && preInputData.get(input);
    }

    public void tick()
    {
        preInputData = curInputData;
        curInputData = inputSource.getInputData();
    }
}
