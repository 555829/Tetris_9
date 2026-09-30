package game.tetris.tetromino;

public enum RotationState
{
    SPAWN,
    RIGHT,
    REVERSE,
    LEFT;

    public RotationState rotateRight()
    {
        switch (this)
        {
            case SPAWN:
                return RIGHT;

            case RIGHT:
                return REVERSE;

            case REVERSE:
                return LEFT;

            case LEFT:
                return SPAWN;

            default:
                throw new IllegalStateException();
        }
    }

    public RotationState rotateLeft()
    {
        switch (this)
        {
            case SPAWN:
                return LEFT;

            case LEFT:
                return REVERSE;

            case REVERSE:
                return RIGHT;

            case RIGHT:
                return SPAWN;

            default:
                throw new IllegalStateException();
        }
    }
}