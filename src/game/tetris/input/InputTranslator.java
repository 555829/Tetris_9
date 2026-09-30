package game.tetris.input;

public class InputTranslator {
    private final IInputSource inputSource;
    private InputData curInputData = new InputData();
    private InputData preInputData = new InputData();

    public InputTranslator(InputSourceType type)
    {
        inputSource = createInputSource(type);
    }

    private IInputSource createInputSource(InputSourceType type) {
        return new KeyboardInputSource(); //TODO : keyboard2, AI 추가
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
