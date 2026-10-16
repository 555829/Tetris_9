package game.ui.multigame.engine.input;

interface IInputSource {
    public InputData getInputData();
    default void close() {}
}
