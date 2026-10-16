package game.ui.multigame.engine.tetrominoQueue;

import game.ui.multigame.engine.tetromino.TetrominoEnum;

import java.util.Random;

import static game.ui.multigame.engine.tetromino.TetrominoEnum.*;

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
