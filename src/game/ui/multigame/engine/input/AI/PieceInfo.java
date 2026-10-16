package game.ui.multigame.engine.input.AI;

import game.ui.multigame.engine.tetromino.RotationState;
import game.ui.multigame.engine.tetromino.TetrominoEnum;

public class PieceInfo
{
    TetrominoEnum tetromino;
    RotationState rotation;
    int x;
    int y;

    float score;
    boolean useHold;

    public PieceInfo() {}

    public PieceInfo(TetrominoEnum tetromino, RotationState rotation, int x, int y)
    {
        this.tetromino = tetromino;
        this.rotation = rotation;
        this.x = x;
        this.y = y;
    }
}
