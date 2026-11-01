package game.ui.multigame.engine.tetrominoQueue;

import game.ui.multigame.engine.tetromino.TetrominoEnum;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static game.ui.multigame.engine.tetromino.TetrominoEnum.*;

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
    private final boolean omitLine;

    public PieceGenerator_7Bag()
    {
        omitLine = false;
    }

    public PieceGenerator_7Bag(int width, int height)
    {
        game.rules.BoardDimensions.validate(width, height);
        omitLine = width == 3 && height == 3;
    }

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
        for (TetrominoEnum type : tetroRange)
            if (!omitLine || type != LineShape)
                bag.add(type);
        Collections.shuffle(bag);
    }
}
