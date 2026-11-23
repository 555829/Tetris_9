package game.tetris;

import game.tetris.flow.KickedPiece;
import game.tetris.tetromino.TetrominoEnum;

public interface ITetrisReader
{
    int getWidth();
    int getHeight();

    TetrominoEnum getBoardCell(int x, int y);
    TetrominoEnum getActivePieceCell(int x, int y);

    TetrominoEnum getHoldPiece();
    TetrominoEnum[] getNextPieces();
    KickedPiece getKickedPiece();
    double getMorphFlashRatio();
    boolean hasActivePiece();

    int getScore();
    int getLevel();
    int getLines();

    boolean isGameOver();
    boolean isMenuOpen();
    boolean isPaused();
}
