package game.tetris.input;

import game.SettingsManager;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Queue;

class KeyboardInputSource implements IInputSource
{
    private final KeyboardFocusManager keyboardManager;
    private final java.awt.KeyEventDispatcher dispatcher;
    private final InputData physicalInput = new InputData();
    private final InputData outputInput = new InputData();
    private final SettingsManager settings;

    private final EnumMap<InputEnum, Queue<Boolean>> inputBuffer =
            new EnumMap<>(InputEnum.class);

    public KeyboardInputSource(SettingsManager settings)
    {
        this.settings = settings;
        for (InputEnum input : InputEnum.values())
        {
            inputBuffer.put(input, new ArrayDeque<>());
        }

        keyboardManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        dispatcher = this::handleKeyEvent;
        keyboardManager.addKeyEventDispatcher(dispatcher);
    }

    @Override
    public synchronized InputData getInputData()
    {
        for (InputEnum input : InputEnum.values())
        {
            Queue<Boolean> buffer = inputBuffer.get(input);

            if (!buffer.isEmpty())
            {
                outputInput.set(input, buffer.poll());
            }
        }

        return outputInput.copy();
    }

    @Override
    public synchronized void close()
    {
        keyboardManager.removeKeyEventDispatcher(dispatcher);
        for (InputEnum input : InputEnum.values())
        {
            physicalInput.set(input, false);
            outputInput.set(input, false);
            inputBuffer.get(input).clear();
        }
    }

    private synchronized boolean handleKeyEvent(KeyEvent event)
    {
        InputEnum input = convertKey(event.getKeyCode());

        if (input == null)
            return false;

        if (event.getID() == KeyEvent.KEY_PRESSED)
        {
            if (!physicalInput.get(input))
            {
                physicalInput.set(input, true);
                inputBuffer.get(input).add(true);
            }
        }
        else if (event.getID() == KeyEvent.KEY_RELEASED)
        {
            if (physicalInput.get(input))
            {
                physicalInput.set(input, false);
                inputBuffer.get(input).add(false);
            }
        }

        return false;
    }

    InputEnum convertKey(int keyCode)
    {
        return convertKey(keyCode, settings);
    }

    static InputEnum convertKey(int keyCode, SettingsManager settings)
    {
        String keyName = keyNameForCode(keyCode);
        if (settings != null) {
            if (matches(keyName, settings.p1Left)) return InputEnum.Left;
            if (matches(keyName, settings.p1Right)) return InputEnum.Right;
            if (matches(keyName, settings.p1Soft)) return InputEnum.OneLineDown;
            if (matches(keyName, settings.p1Hard)) return InputEnum.DropDown;
            if (matches(keyName, settings.p1Rotation)) return InputEnum.Rotate;
        }

        // 설정에 없는 시스템 키는 이전 기본 동작을 계속 지원합니다.
        switch (keyCode) {
            case KeyEvent.VK_E: return InputEnum.Hold;
            case KeyEvent.VK_P: return InputEnum.Pause;
            case KeyEvent.VK_ESCAPE: return InputEnum.Menu;
            default: return null;
        }
    }

    private static boolean matches(String keyName, String configuredKey) {
        return configuredKey != null && keyName.equals(normalize(configuredKey));
    }

    private static String keyNameForCode(int keyCode) {
        switch (keyCode) {
            case KeyEvent.VK_LEFT: return "LEFT";
            case KeyEvent.VK_RIGHT: return "RIGHT";
            case KeyEvent.VK_UP: return "UP";
            case KeyEvent.VK_DOWN: return "DOWN";
            case KeyEvent.VK_SPACE: return "SPACE";
            default: return normalize(KeyEvent.getKeyText(keyCode));
        }
    }
    private static String normalize(String keyName) {
        return keyName == null ? "" : keyName.trim().toUpperCase(Locale.ROOT);
    }
}
