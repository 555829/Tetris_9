package game.tetris.input;

import java.util.EnumSet;

final class InputData {
    private final EnumSet<InputEnum> inputs =
            EnumSet.noneOf(InputEnum.class);

    public boolean get(InputEnum input) {
        return inputs.contains(input);
    }

    public void set(InputEnum input, boolean value) {
        if (value) {
            inputs.add(input);
        } else {
            inputs.remove(input);
        }
    }
    InputData copy() {
        InputData copy = new InputData();
        copy.inputs.addAll(inputs);

        return copy;
    }
}