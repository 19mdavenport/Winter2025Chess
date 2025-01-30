package chess.moves.extra;

import chess.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

public class CastlingMoveCalculator implements ExtraMoveCalculator {

    private boolean[] castlingOptions;

    public CastlingMoveCalculator() {
        castlingOptions = new boolean[4];
        Arrays.fill(castlingOptions, true);
    }


    public void setBoard(ChessBoard board) {
        ChessPiece whiteKing = board.getPiece(new ChessPosition(1, 5));
        ChessPiece whiteRookK = board.getPiece(new ChessPosition(1, 8));
        ChessPiece whiteRookQ = board.getPiece(new ChessPosition(1, 1));

        if (whiteKing == null || whiteKing.getPieceType() != ChessPiece.PieceType.KING) {
            castlingOptions[0] = false;
            castlingOptions[1] = false;
        } else {
            castlingOptions[0] = (whiteRookK != null && whiteRookK.getPieceType() == ChessPiece.PieceType.ROOK);
            castlingOptions[1] = (whiteRookQ != null && whiteRookQ.getPieceType() == ChessPiece.PieceType.ROOK);
        }

        ChessPiece blackKing = board.getPiece(new ChessPosition(8, 5));
        ChessPiece blackRookK = board.getPiece(new ChessPosition(8, 8));
        ChessPiece blackRookQ = board.getPiece(new ChessPosition(8, 1));

        if (blackKing == null || blackKing.getPieceType() != ChessPiece.PieceType.KING) {
            castlingOptions[2] = false;
            castlingOptions[3] = false;
        } else {
            castlingOptions[2] = (blackRookK != null && blackRookK.getPieceType() == ChessPiece.PieceType.ROOK);
            castlingOptions[3] = (blackRookQ != null && blackRookQ.getPieceType() == ChessPiece.PieceType.ROOK);
        }
    }


    public void moveMade(ChessMove move, ChessBoard board) {
        ChessPiece piece = board.getPiece(move.getEndPosition());
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (piece.getTeamColor() == ChessGame.TeamColor.WHITE) {
                castlingOptions[0] = false;
                castlingOptions[1] = false;
            } else {
                castlingOptions[2] = false;
                castlingOptions[3] = false;
            }
        } else if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            ChessPosition startPos = move.getStartPosition();
            if (castlingOptions[0] && startPos.getRow() == 1 && startPos.getColumn() == 8) {
                castlingOptions[0] = false;
            }
            if (castlingOptions[1] && startPos.getRow() == 1 && startPos.getColumn() == 1) {
                castlingOptions[1] = false;
            }
            if (castlingOptions[2] && startPos.getRow() == 8 && startPos.getColumn() == 8) {
                castlingOptions[2] = false;
            }
            if (castlingOptions[3] && startPos.getRow() == 8 && startPos.getColumn() == 1) {
                castlingOptions[3] = false;
            }
        }
    }


    @Override
    public boolean moveMatches(ChessMove move, ChessBoard board) {
        ChessPiece piece = board.getPiece(move.getStartPosition());
        return (piece.getPieceType() == ChessPiece.PieceType.KING &&
                Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) == 2);
    }


    public Collection<ChessMove> validMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> ret = new HashSet<>();

        ChessPiece piece = board.getPiece(position);

        if (piece == null || piece.getPieceType() != ChessPiece.PieceType.KING || position.getColumn() != 5 ||
                ((piece.getTeamColor() == ChessGame.TeamColor.WHITE && position.getRow() != 1) ||
                        (piece.getTeamColor() == ChessGame.TeamColor.BLACK && position.getRow() != 8))) {
            return ret;
        }

        int offset = piece.getTeamColor() == ChessGame.TeamColor.WHITE ? 0 : 2;

        if (castlingOptions[offset]) {
            ChessMove kingSideCastle = singleCastlingMove(board, piece.getTeamColor(), true);
            if (kingSideCastle != null) {
                ret.add(kingSideCastle);
            }
        }
        if (castlingOptions[offset + 1]) {
            ChessMove queenSideCastle = singleCastlingMove(board, piece.getTeamColor(), false);
            if (queenSideCastle != null) {
                ret.add(queenSideCastle);
            }
        }

        return ret;
    }


    public void performMove(ChessMove move, ChessBoard board) throws InvalidMoveException {
        if (move.getPromotionPiece() != null ||
                board.getPiece(move.getStartPosition()).getPieceType() != ChessPiece.PieceType.KING ||
                Math.abs(move.getStartPosition().getColumn() - move.getEndPosition().getColumn()) != 2) {
            throw new InvalidMoveException("Not a valid castling move");
        }
        int oldColumn = (move.getStartPosition().getColumn() > move.getEndPosition().getColumn()) ? 1 : 8;
        ChessPosition oldPosition = new ChessPosition(move.getEndPosition().getRow(), oldColumn);

        ChessPosition newPosition = new ChessPosition(move.getEndPosition().getRow(),
                (move.getStartPosition().getColumn() + move.getEndPosition().getColumn()) / 2);

        board.addPiece(newPosition, board.getPiece(oldPosition));
        board.addPiece(oldPosition, null);

        ChessPiece king = board.getPiece(move.getStartPosition());
        board.addPiece(move.getStartPosition(), null);
        board.addPiece(move.getEndPosition(), king);
    }


    private ChessMove singleCastlingMove(ChessBoard board, ChessGame.TeamColor color, boolean kingSide) {
        if (ChessGame.isInCheck(color, board)) {
            return null;
        }

        int row = (color == ChessGame.TeamColor.WHITE) ? 1 : 8;
        int col = (kingSide) ? 6 : 4;

        for (int i = col; i < 8 && i > 1; i += col - 5) {
            if (board.getPiece(new ChessPosition(row, i)) != null) {
                return null;
            }
        }

        ChessPosition orig = new ChessPosition(row, 5);
        ChessBoard betweenBoard = new ChessBoard(board);
        betweenBoard.addPiece(new ChessPosition(row, col), betweenBoard.getPiece(orig));
        betweenBoard.addPiece(orig, null);
        if (ChessGame.isInCheck(color, betweenBoard)) {
            return null;
        }

        ChessPosition end = new ChessPosition(row, 5 + 2 * (col - 5));
        ChessMove out = new ChessMove(orig, end);
        ChessBoard outBoard = new ChessBoard(board);
        try {
            performMove(out, outBoard);
        } catch (InvalidMoveException e) {
            return null;
        }
        return (ChessGame.isInCheck(color, outBoard)) ? null : out;
    }


    @Override
    public int hashCode() {
        return Arrays.hashCode(castlingOptions);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CastlingMoveCalculator that = (CastlingMoveCalculator) o;

        return Arrays.equals(castlingOptions, that.castlingOptions);
    }


    @Override
    public String toString() {
        return new StringBuilder()
                .append((castlingOptions[0] ? 'K' : '-'))
                .append((castlingOptions[1]) ? 'Q' : '-')
                .append((castlingOptions[2]) ? 'k' : '-')
                .append((castlingOptions[3]) ? 'q' : '-')
                .toString();
    }

    @Override
    public CastlingMoveCalculator clone() throws CloneNotSupportedException {
        CastlingMoveCalculator clone = (CastlingMoveCalculator) super.clone();
        clone.castlingOptions = Arrays.copyOf(castlingOptions, castlingOptions.length);
        return clone;
    }

}
