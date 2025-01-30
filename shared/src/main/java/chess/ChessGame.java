package chess;

import chess.moves.extra.CastlingMoveCalculator;
import chess.moves.extra.EnPassantMoveCalculator;
import chess.moves.extra.ExtraMoveCalculator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;

    private TeamColor teamTurn;

    boolean active;

    private final List<ExtraMoveCalculator> extraMoveCalculators;


    public ChessGame() {
        board = new ChessBoard();
        teamTurn = TeamColor.WHITE;

        board.resetBoard();
        active = true;
        extraMoveCalculators = List.of(new CastlingMoveCalculator(), new EnPassantMoveCalculator());
    }

    public ChessGame(ChessGame copy) {
        this.board = new ChessBoard(copy.board);
        this.teamTurn = copy.teamTurn;
        this.active = copy.active;
        this.extraMoveCalculators = new ArrayList<>(copy.extraMoveCalculators.size());
        for(int i = 0; i < copy.extraMoveCalculators.size(); i++) {
            try {
                extraMoveCalculators.add(i, copy.extraMoveCalculators.get(i).clone());
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }


    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }


    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE, BLACK;


        public TeamColor getOpposite() {
            return (this == WHITE) ? BLACK : WHITE;
        }
    }


    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        }

        //Get all possible moves for the piece
        Collection<ChessMove> moves = piece.pieceMoves(board, startPosition);

        for (ExtraMoveCalculator extraMoveCalculator : extraMoveCalculators) {
            moves.addAll(extraMoveCalculator.validMoves(board, startPosition));
        }

        //If making the move resulted in the king being placed in check, it's not legal
        moves.removeIf(move -> isMoveInvalid(move, board));
        return moves;
    }


    public boolean isMoveInvalid(ChessMove move, ChessBoard board) {
        try {
            ChessBoard copyBoard = new ChessBoard(board);
            ChessPiece movingPiece = copyBoard.getPiece(move.getStartPosition());
            performMove(move, copyBoard);
            return isInCheck(movingPiece.getTeamColor(), copyBoard);
        } catch (InvalidMoveException e) {
            return true;
        }
    }


    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //If there is no piece at the starting location or if said piece is the wrong color, the move is invalid
        if (piece == null) {
            throw new InvalidMoveException("No piece at starting position");
        }
        else if (piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("Piece of wrong color for turn");
        }
        else {
            //If the piece can't actually make that move, it's invalid
            Collection<ChessMove> moves = validMoves(move.getStartPosition());
            if (!moves.contains(move)) {
                throw new InvalidMoveException("Not a valid move");
            }
        }

        performMove(move, board);

        for (ExtraMoveCalculator extraMoveCalculator : extraMoveCalculators) {
            extraMoveCalculator.moveMade(move, board);
        }

        teamTurn = teamTurn.getOpposite();
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }


    public static boolean isInCheck(TeamColor teamColor, ChessBoard board) {
        ChessPosition king = null;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor &&
                        piece.getPieceType() == ChessPiece.PieceType.KING) {
                    king = pos;
                    break;
                }
            }
        }

        if (king == null) {
            return false;
        }

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                if(positionThreatensKing(new ChessPosition(i, j), board, king, teamColor)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean positionThreatensKing(ChessPosition position, ChessBoard board,
                                                 ChessPosition kingPosition, TeamColor teamColor) {
        ChessPiece piece = board.getPiece(position);
        if (piece != null && piece.getTeamColor() != teamColor) {
            for (ChessMove move : piece.pieceMoves(board, position)) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noValidMovesExist(teamColor);
    }


    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMovesExist(teamColor);
    }


    private boolean noValidMovesExist(TeamColor teamColor) {
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = board.getPiece(pos);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    if (!validMoves(pos).isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        for (ExtraMoveCalculator extraMoveCalculator : extraMoveCalculators) {
            extraMoveCalculator.setBoard(board);
        }
    }


    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }


    private void performMove(ChessMove move, ChessBoard board) throws InvalidMoveException {
        ChessPiece piece = board.getPiece(move.getStartPosition());

        //If there is no piece at the starting location or if said piece is the wrong color, the move is invalid
        if (piece == null) {
            throw new InvalidMoveException("No piece at starting position");
        }

        for (ExtraMoveCalculator extraMoveCalculator : extraMoveCalculators) {
            if (extraMoveCalculator.moveMatches(move, board)) {
                extraMoveCalculator.performMove(move, board);
                return;
            }
        }

        if (move.getPromotionPiece() == null) {
            piece = board.getPiece(move.getStartPosition());
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), piece);
        } else { //Promotions
            if (piece.getPieceType() != ChessPiece.PieceType.PAWN) {
                throw new InvalidMoveException("Move with promotion piece not on pawn");
            }
            if ((piece.getTeamColor() == TeamColor.WHITE && move.getEndPosition().getRow() != 8) ||
                    (piece.getTeamColor() == TeamColor.BLACK && move.getEndPosition().getRow() != 1)) {
                throw new InvalidMoveException("Move with promotion piece doesn't end to correct end of ret");
            }
            if (board.getPiece(move.getEndPosition()) != null) {
                board.addPiece(move.getEndPosition(), null);
            }

            TeamColor color = board.getPiece(move.getStartPosition()).getTeamColor();
            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), new ChessPiece(color, move.getPromotionPiece()));
        }
    }


    @Override
    public String toString() {
        return String.format("%s %s", board.toString(), (teamTurn == TeamColor.WHITE) ? 'w' : 'b');
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamTurn == chessGame.teamTurn;
    }


    @Override
    public int hashCode() {
        int result = board != null ? board.hashCode() : 0;
        result = 31 * result + (teamTurn != null ? teamTurn.hashCode() : 0);
        return result;
    }

}
