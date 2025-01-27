package chess.moves.piece;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public class PawnMovesCalculator implements PieceMovesCalculator {
    private static final int[] CAPTURE_DIRECTIONS = new int[] {1, -1};

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        Collection<ChessMove> moves = new HashSet<>();

        int single = myColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        int start = myColor == ChessGame.TeamColor.WHITE ? 2 : ChessBoard.BOARD_SIZE - 1;
        int end = myColor == ChessGame.TeamColor.WHITE ? ChessBoard.BOARD_SIZE : 1;

        ChessPosition forward = new ChessPosition(myPosition.getRow() + single, myPosition.getColumn());
        ChessPiece forwardPiece = board.getPiece(forward);
        if (forwardPiece == null) {
            add(moves, myPosition, forward, end);

            if (myPosition.getRow() == start) {
                ChessPosition doublePos = new ChessPosition(myPosition.getRow() + 2 * single, myPosition.getColumn());
                ChessPiece doublePiece = board.getPiece(doublePos);
                if (doublePiece == null) {
                    add(moves, myPosition, doublePos, end);
                }
            }
        }

        for (int direction : CAPTURE_DIRECTIONS) {
            if (isInBounds(myPosition.getRow() + single, myPosition.getColumn() - direction)) {
                ChessPosition takePos = new ChessPosition(myPosition.getRow() + single, myPosition.getColumn() - direction);
                ChessPiece takePiece = board.getPiece(takePos);

                if (takePiece != null && takePiece.getTeamColor() != myColor) {
                    add(moves, myPosition, takePos, end);
                }
            }
        }

        return moves;
    }

    private void add(Collection<ChessMove> moves, ChessPosition startPosition, ChessPosition endPosition, int endRow) {
        if (endPosition.getRow() == endRow) {
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.QUEEN));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.ROOK));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.BISHOP));
            moves.add(new ChessMove(startPosition, endPosition, ChessPiece.PieceType.KNIGHT));
        } else {
            moves.add(new ChessMove(startPosition, endPosition, null));
        }
    }
}
