package game.tetris.tetrominoQueue;

import game.tetris.tetromino.TetrominoEnum;

import java.util.Random;

import static game.tetris.tetromino.TetrominoEnum.*;

public class PieceGenerator_Random implements PieceGenerator
{
    private final Random random = new Random();
    private TetrominoEnum[] tetroRange = {
            ZShape,
            SShape,
            LineShape,
            TShape,
            SquareShape,
            LShape,
            MirroredLShape
    };

    @Override
    public TetrominoEnum getPiece() {
        return tetroRange[random.nextInt(tetroRange.length)];
    }
}
