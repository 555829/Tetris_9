package game.tetris.input;

import game.SettingsManager;
import game.tetris.flow.TetrisFlow;

public class InputTranslator {
    private final IInputSource inputSource;
    private final TetrisFlow flow;
    private final SettingsManager settings;
    private InputData curInputData = new InputData();
    private InputData preInputData = new InputData();

    public InputTranslator(InputSourceType type, TetrisFlow flow)
    {
        this(type, flow, null);
    }

    public InputTranslator(InputSourceType type, TetrisFlow flow, SettingsManager settings)
    {
        this.flow = flow;
        this.settings = settings;
        inputSource = createInputSource(type);
    }

    // 입력 종류마다 실제 키보드, AI 또는 원격 보드 입력 공급자를 연결합니다.
    private IInputSource createInputSource(InputSourceType type) {
        switch (type)
        {
            case Keyboard1 :
                return new KeyboardInputSource(settings);
            case AI:
                return new AIInputSource(flow, 5);
            case Keyboard2:
                return new RemoteInputSource();
            default:
                throw new IllegalArgumentException("Invalid InputSourceType");
        }
    }



    // 현재 입력과 이전 입력을 비교해 눌림 유지, 최초 눌림, 해제 상태를 구분합니다.
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
    public void close()
    {
        inputSource.close();
    }
}
