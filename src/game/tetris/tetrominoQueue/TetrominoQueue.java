package game.tetris.tetrominoQueue;

import game.tetris.tetromino.TetrominoEnum;

import java.util.*;

public class TetrominoQueue {
    private final PieceGenerator pieceGenerator;
    private static final int size = 5;
    public static int getSize(){ return size;}
    private final Queue<TetrominoEnum> tetroQueue = new ArrayDeque<>();


    public TetrominoQueue(PieceGenerator pieceGenerator)
    {
        this.pieceGenerator = pieceGenerator;
        reset();
    }
    public TetrominoEnum getNextTetromino()
    {
        tetroQueue.add(pieceGenerator.getPiece());
        return tetroQueue.poll();
    }
    public TetrominoEnum peekNextTetromino()
    {
        return tetroQueue.peek();
    }
    public void reset()
    {
        tetroQueue.clear();

        for (int i = 0; i < size; i++)
            tetroQueue.add(pieceGenerator.getPiece());
    }


    // UI 용
    public TetrominoEnum[] getQueue() {
        return tetroQueue.toArray(new TetrominoEnum[0]);
    }

}
