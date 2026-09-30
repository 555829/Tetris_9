package game.tetris.tetromino;

public final class TetrominoShape
{
    public static final int SIZE = 4;

    private final TetrominoEnum type;
    private final RotationState rotation;
    //TetrominoShapeData.SIZE ^ 2 배열
    private final TetrominoEnum[][] shape;


    public TetrominoShape(TetrominoEnum type)
    {
        this(type, RotationState.SPAWN);
    }
    private TetrominoShape(TetrominoEnum type, RotationState rotation)
    {
        this.type = type;
        this.rotation = rotation;

        shape = TetrominoShapeData.getShape(type, rotation);
    }

    //새 객체 반환
    public TetrominoShape rotateRight()
    {return new TetrominoShape(type, rotation.rotateRight());}
    public TetrominoShape rotateLeft()
    {return new TetrominoShape(type, rotation.rotateLeft());}


    public TetrominoEnum get(int x, int y)
    {return shape[y][x];}

    public TetrominoEnum[][] getShape()
    {return copyOf(shape);}

    public TetrominoEnum getType()
    {return type;}
    public RotationState getRotation()
    {return rotation;}



    private static TetrominoEnum[][] copyOf(TetrominoEnum[][] source)
    {
        TetrominoEnum[][] result =
                new TetrominoEnum[source.length][];

        for (int y = 0; y < source.length; y++)
        {
            result[y] = source[y].clone();
        }

        return result;
    }
}