package chess;

import java.util.Arrays;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    public static final int BOARD_SIZE = 8;

    private static final ChessPiece.PieceType[] BACK_ROW_TYPES = {
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.KING,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT,
            ChessPiece.PieceType.ROOK
    };

    private final ChessPiece[][] board;


    public ChessBoard() {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
    }


    /**
     * Copy constructor. Constructs a board with pieces the same as provided board
     *
     * @param copy ChessBoard to copy the pieces of
     */
    public ChessBoard(ChessBoard copy) {
        board = new ChessPiece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[i] = Arrays.copyOf(copy.board[i], BOARD_SIZE);
        }
    }


    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow() - 1][position.getColumn() - 1] = piece;
    }


    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow() - 1][position.getColumn() - 1];
    }


    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int i = 0; i < BOARD_SIZE; i++) {
            board[0][i] = new ChessPiece(ChessGame.TeamColor.WHITE, BACK_ROW_TYPES[i]);
            board[1][i] = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
            for(int j = 2; j < BOARD_SIZE - 2; j++) {
                board[j][i] = null;
            }
            board[BOARD_SIZE - 2][i] = new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN);
            board[BOARD_SIZE - 1][i] = new ChessPiece(ChessGame.TeamColor.BLACK, BACK_ROW_TYPES[i]);
        }
    }


    @Override
    public String toString() { // FEN
        StringBuilder builder = new StringBuilder();
        for (int i = BOARD_SIZE - 1; i >= 0; i--) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                builder.append(board[i][j] == null ? "1" : board[i][j].toString());
            }
            builder.append('/');
        }
        builder.deleteCharAt(builder.length() - 1); // delete trailing slash

        int i = 0;
        while (i < builder.length() - 1) {
            char first = builder.charAt(i);
            char second = builder.charAt(i + 1);
            if (Character.isDigit(first) && Character.isDigit(second)) {
                int firstInt = Integer.parseInt(String.valueOf(first));
                int secondInt = Integer.parseInt(String.valueOf(second));
                int replace = firstInt + secondInt;
                builder.replace(i, i + 2, String.valueOf(replace));
            } else {
                i++;
            }
        }

        return builder.toString();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return Arrays.deepEquals(this.board, ((ChessBoard) o).board);
    }


    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

}
