package game.tetris.input;

import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.EnumMap;
import java.util.Queue;

class KeyboardInputSource implements IInputSource
{
    private final InputData physicalInput = new InputData();
    private final InputData outputInput = new InputData();

    private final EnumMap<InputEnum, Queue<Boolean>> inputBuffer =
            new EnumMap<>(InputEnum.class);

    public KeyboardInputSource()
    {
        for (InputEnum input : InputEnum.values())
        {
            inputBuffer.put(input, new ArrayDeque<>());
        }

        KeyboardFocusManager
                .getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(this::handleKeyEvent);
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

    private InputEnum convertKey(int keyCode)
    {
        switch (keyCode)
        {
            case KeyEvent.VK_LEFT:
                return InputEnum.Left;

            case KeyEvent.VK_RIGHT:
                return InputEnum.Right;

            case KeyEvent.VK_UP:
                return InputEnum.Rotate;

            case KeyEvent.VK_SPACE:
                return InputEnum.DropDown;

            case KeyEvent.VK_DOWN:
                return InputEnum.OneLineDown;

            case KeyEvent.VK_E:
                return InputEnum.Hold;

            case KeyEvent.VK_P:
                return InputEnum.Pause;

            case KeyEvent.VK_ESCAPE:
                return InputEnum.Menu;

            default:
                return null;
        }
    }
}