package game.tetris.tetromino;

final class TetrominoShapeData
{
    private static final int SIZE = 4;

    // 최중요 API!!!!!!!!!!!!!!!!!!!!
    static TetrominoEnum[][] getShape(TetrominoEnum type, RotationState rotation)
    {
        TetrominoEnum[][][] data;

        switch (type)
        {
            case ZShape:
                data = Z;
                break;
            case SShape:
                data = S;
                break;
            case LineShape:
                data = I;
                break;
            case TShape:
                data = T;
                break;
            case SquareShape:
                data = O;
                break;
            case LShape:
                data = L;
                break;
            case MirroredLShape:
                data = J;
                break;
            case NoShape:
                return grid(
                        TetrominoEnum.NoShape,
                        "....", "....", "....", "....");
            default:
                throw new IllegalArgumentException();
        }

        int index;

        switch (rotation)
        {
            case SPAWN:
                index = 0;
                break;
            case RIGHT:
                index = 1;
                break;
            case REVERSE:
                index = 2;
                break;
            case LEFT:
                index = 3;
                break;
            default:
                throw new IllegalArgumentException();
        }

        return copyOf(data[index]);
    }


    // Z
    private static final TetrominoEnum[][][] Z = {
            grid(TetrominoEnum.ZShape,
                    "XX..",
                    ".XX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.ZShape,
                    "..X.",
                    ".XX.",
                    ".X..",
                    "...."),

            grid(TetrominoEnum.ZShape,
                    "....",
                    "XX..",
                    ".XX.",
                    "...."),

            grid(TetrominoEnum.ZShape,
                    ".X..",
                    "XX..",
                    "X...",
                    "....")
    };

    // S
    private static final TetrominoEnum[][][] S = {
            grid(TetrominoEnum.SShape,
                    ".XX.",
                    "XX..",
                    "....",
                    "...."),

            grid(TetrominoEnum.SShape,
                    ".X..",
                    ".XX.",
                    "..X.",
                    "...."),

            grid(TetrominoEnum.SShape,
                    "....",
                    ".XX.",
                    "XX..",
                    "...."),

            grid(TetrominoEnum.SShape,
                    "X...",
                    "XX..",
                    ".X..",
                    "....")
    };

    // I
    private static final TetrominoEnum[][][] I = {
            grid(TetrominoEnum.LineShape,
                    "....",
                    "XXXX",
                    "....",
                    "...."),

            grid(TetrominoEnum.LineShape,
                    "..X.",
                    "..X.",
                    "..X.",
                    "..X."),

            grid(TetrominoEnum.LineShape,
                    "....",
                    "....",
                    "XXXX",
                    "...."),

            grid(TetrominoEnum.LineShape,
                    ".X..",
                    ".X..",
                    ".X..",
                    ".X..")
    };

    // T
    private static final TetrominoEnum[][][] T = {
            grid(TetrominoEnum.TShape,
                    ".X..",
                    "XXX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.TShape,
                    ".X..",
                    ".XX.",
                    ".X..",
                    "...."),

            grid(TetrominoEnum.TShape,
                    "....",
                    "XXX.",
                    ".X..",
                    "...."),

            grid(TetrominoEnum.TShape,
                    ".X..",
                    "XX..",
                    ".X..",
                    "....")
    };

    // O
    private static final TetrominoEnum[][][] O = {
            grid(TetrominoEnum.SquareShape,
                    ".XX.",
                    ".XX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.SquareShape,
                    ".XX.",
                    ".XX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.SquareShape,
                    ".XX.",
                    ".XX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.SquareShape,
                    ".XX.",
                    ".XX.",
                    "....",
                    "....")
    };

    // L
    private static final TetrominoEnum[][][] L = {
            grid(TetrominoEnum.LShape,
                    "..X.",
                    "XXX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.LShape,
                    ".X..",
                    ".X..",
                    ".XX.",
                    "...."),

            grid(TetrominoEnum.LShape,
                    "....",
                    "XXX.",
                    "X...",
                    "...."),

            grid(TetrominoEnum.LShape,
                    "XX..",
                    ".X..",
                    ".X..",
                    "....")
    };

    // J
    private static final TetrominoEnum[][][] J = {
            grid(TetrominoEnum.MirroredLShape,
                    "X...",
                    "XXX.",
                    "....",
                    "...."),

            grid(TetrominoEnum.MirroredLShape,
                    ".XX.",
                    ".X..",
                    ".X..",
                    "...."),

            grid(TetrominoEnum.MirroredLShape,
                    "....",
                    "XXX.",
                    "..X.",
                    "...."),

            grid(TetrominoEnum.MirroredLShape,
                    ".X..",
                    ".X..",
                    "XX..",
                    "....")
    };




    // 2차원 배열 복사
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

    // 문자열을 4×4 배열로 변환
    private static TetrominoEnum[][] grid(
            TetrominoEnum type,
            String... rows)
    {
        if (rows.length != SIZE)
            throw new IllegalArgumentException();

        TetrominoEnum[][] result =
                new TetrominoEnum[SIZE][SIZE];

        for (int y = 0; y < SIZE; y++)
        {
            if (rows[y].length() != SIZE)
                throw new IllegalArgumentException();

            for (int x = 0; x < SIZE; x++)
            {
                char cell = rows[y].charAt(x);

                if (cell == 'X')
                    result[y][x] = type;
                else if (cell == '.')
                    result[y][x] = TetrominoEnum.NoShape;
                else
                    throw new IllegalArgumentException();
            }
        }

        return result;
    }
}