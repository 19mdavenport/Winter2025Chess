package chess.moves.piece;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

import java.util.Collection;

public interface PieceMovesCalculator {

    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor);

    default boolean isInBounds(int i) {
        return i >= 1 && i <= ChessBoard.BOARD_SIZE;
    }

    default boolean isInBounds(int row, int column) {
        return isInBounds(row) && isInBounds(column);
    }

}
