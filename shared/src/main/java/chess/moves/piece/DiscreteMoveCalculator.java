package chess.moves.piece;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public abstract class DiscreteMoveCalculator implements PieceMovesCalculator {
    private final int[][] directions;

    protected DiscreteMoveCalculator(int[][] directions) {
        this.directions = directions;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        Collection<ChessMove> moves = new HashSet<>();

        for(int[] direction : directions) {
            int row = myPosition.getRow() + direction[0];
            int col = myPosition.getColumn() + direction[1];
            if (!isInBounds(row, col)) continue;

            ChessPosition endPosition = new ChessPosition(row, col);
            ChessPiece piece = board.getPiece(endPosition);
            if(piece == null || piece.getTeamColor() != myColor) {
                moves.add(new ChessMove(myPosition, endPosition, null));
            }
        }

        return moves;
    }
}
