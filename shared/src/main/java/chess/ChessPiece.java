package chess;

import chess.moves.piece.*;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private static final Map<PieceType, PieceMovesCalculator> CALCULATORS = new EnumMap<>(PieceType.class);

    static {
        CALCULATORS.put(PieceType.KING, new KingMovesCalculator());
        CALCULATORS.put(PieceType.QUEEN, new QueenMovesCalculator());
        CALCULATORS.put(PieceType.BISHOP, new BishopMovesCalculator());
        CALCULATORS.put(PieceType.KNIGHT, new KnightMovesCalculator());
        CALCULATORS.put(PieceType.ROOK, new RookMovesCalculator());
        CALCULATORS.put(PieceType.PAWN, new PawnMovesCalculator());
    }

    private final ChessGame.TeamColor teamColor;

    private final PieceType pieceType;


    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        teamColor = pieceColor;
        pieceType = type;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return CALCULATORS.get(pieceType).pieceMoves(board, myPosition, teamColor);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }

    @Override
    public String toString() {
        char c = switch (pieceType) {
            case KING -> 'k';
            case QUEEN -> 'q';
            case BISHOP -> 'b';
            case KNIGHT -> 'n';
            case ROOK -> 'r';
            case PAWN -> 'p';
        };
        return switch (teamColor) {
            case WHITE -> String.valueOf(c).toUpperCase();
            case BLACK -> String.valueOf(c).toLowerCase();
        };
    }
}
