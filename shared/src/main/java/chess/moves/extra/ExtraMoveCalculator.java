package chess.moves.extra;

import chess.ChessBoard;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;

import java.util.Collection;

public interface ExtraMoveCalculator extends Cloneable {
    void setBoard(ChessBoard board);

    void moveMade(ChessMove move, ChessBoard board);

    boolean moveMatches(ChessMove move, ChessBoard board);

    Collection<ChessMove> validMoves(ChessBoard board, ChessPosition position);

    void performMove(ChessMove move, ChessBoard board) throws InvalidMoveException;

    ExtraMoveCalculator clone() throws CloneNotSupportedException;
}
