package chess.moves.piece;

import chess.*;

import java.util.Collection;
import java.util.HashSet;

public abstract class LineMovesCalculator implements PieceMovesCalculator {
    private final int[][] directions;

    protected LineMovesCalculator(int[][] directions) {
        this.directions = directions;
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition, ChessGame.TeamColor myColor) {
        Collection<ChessMove> moves = new HashSet<>();

        for(int[] direction: directions) {
            ChessPosition newPos = myPosition;
            while (isInBounds(newPos.getRow() + direction[0], newPos.getColumn() + direction[1])) {
                newPos = new ChessPosition(newPos.getRow() + direction[0], newPos.getColumn() + direction[1]);
                ChessPiece newPosPiece = board.getPiece(newPos);
                if (newPosPiece == null) {
                    moves.add(new ChessMove(myPosition, newPos));
                } else {
                    if (newPosPiece.getTeamColor() != myColor) {
                        moves.add(new ChessMove(myPosition, newPos));
                    }
                    break;
                }
            }
        }

        return moves;
    }
}
