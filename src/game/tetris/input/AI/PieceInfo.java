package game.tetris.input.AI;

import game.tetris.tetromino.RotationState;
import game.tetris.tetromino.TetrominoEnum;

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
