package game.tetris.tetrominoQueue;

import game.tetris.tetromino.TetrominoEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static game.tetris.tetromino.TetrominoEnum.*;

public class PieceGenerator_7Bag implements PieceGenerator
{
    private final TetrominoEnum[] tetroRange = {
            ZShape,
            SShape,
            LineShape,
            TShape,
            SquareShape,
            LShape,
            MirroredLShape
    };

    private final List<TetrominoEnum> bag = new ArrayList<>();

    @Override
    public TetrominoEnum getPiece()
    {
        if (bag.isEmpty())
        {
            refillBag();
        }

        return bag.remove(bag.size() - 1);
    }

    private void refillBag()
    {
        Collections.addAll(bag, tetroRange);
        Collections.shuffle(bag);
    }
}