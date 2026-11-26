package game.tetris;

import game.tetris.flow.KickedPiece;
import game.tetris.tetromino.TetrominoEnum;

public class Tetris_Remote implements ITetris
{

    @Override
    public void tick() {

    }

    @Override
    public void close() {

    }

    @Override
    public void markGameOver() {

    }

    @Override
    public void setSystemControlsEnabled(boolean enabled) {

    }

    @Override
    public boolean applyBoardSnapshot(String snapshot) {
        return false;
    }

    @Override
    public void openMenu() {

    }

    @Override
    public int getWidth() {
        return 0;
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public TetrominoEnum getBoardCell(int x, int y) {
        return null;
    }

    @Override
    public TetrominoEnum getActivePieceCell(int x, int y) {
        return null;
    }

    @Override
    public TetrominoEnum getHoldPiece() {
        return null;
    }

    @Override
    public TetrominoEnum[] getNextPieces() {
        return new TetrominoEnum[0];
    }

    @Override
    public KickedPiece getKickedPiece() {
        return null;
    }

    @Override
    public double getMorphFlashRatio() {
        return 0;
    }

    @Override
    public boolean hasActivePiece() {
        return false;
    }

    @Override
    public int getScore() {
        return 0;
    }

    @Override
    public int getLevel() {
        return 0;
    }

    @Override
    public int getLines() {
        return 0;
    }

    @Override
    public boolean isGameOver() {
        return false;
    }

    @Override
    public boolean isMenuOpen() {
        return false;
    }

    @Override
    public boolean isPaused() {
        return false;
    }
}
