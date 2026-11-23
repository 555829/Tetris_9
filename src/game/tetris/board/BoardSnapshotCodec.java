package game.tetris.board;

import game.tetris.ITetrisReader;
import game.tetris.Tetris;
import game.tetris.tetromino.TetrominoEnum;

//======================================================================================================================
// 멀티플레이 보드 상태를 서버 프로토콜 문자열로 변환 (까지는 내가 , 검증하는 코드는 ai가 짰음)
//======================================================================================================================
public final class BoardSnapshotCodec {
    private BoardSnapshotCodec() {
    }

    public static String encode(ITetrisReader tetris) {
        int width = tetris.getWidth();
        int height = tetris.getHeight();
        StringBuilder snapshot = new StringBuilder(width * height);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                TetrominoEnum cell = tetris.hasActivePiece()
                        ? tetris.getActivePieceCell(x, y)
                        : TetrominoEnum.NoShape;
                if (cell == TetrominoEnum.NoShape) cell = tetris.getBoardCell(x, y);
                snapshot.append((char) ('A' + cell.ordinal()));
            }
        }
        return snapshot.toString();
    }
    public static boolean apply(Tetris tetris, String snapshot) {
        return tetris.applyBoardSnapshot(snapshot);
    }
}
