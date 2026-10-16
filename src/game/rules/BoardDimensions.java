package game.rules;

/** Board size limits for both engines; UI integration belongs to its owner. */
public final class BoardDimensions {
    public static final int MIN = 3;
    public static final int MAX = 20;
    public static final int DEFAULT_WIDTH = 10;
    public static final int DEFAULT_HEIGHT = 20;

    private BoardDimensions() { }

    public static void validate(int width, int height) {
        if (width < MIN || width > MAX || height < MIN || height > MAX)
            throw new IllegalArgumentException("Board width and height must be between 3 and 20");
    }
}
