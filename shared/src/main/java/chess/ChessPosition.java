package chess;

import java.util.Objects;

/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private static final int COLUMN_CHAR_OFFSET = 96;

    private final int row;

    private final int column;


    /**
     * Constructs a new Chess position for provided location
     *
     * @param row Row of the board this position is at
     * @param col Col of the board this position is at
     */
    public ChessPosition(int row, int col) {
        if (row < 1 || col < 1 || row > 8 || col > 8) {
            throw new IllegalArgumentException(String.format("%d, %d is not on the board", row, col));
        }
        this.row = row;
        this.column = col;
    }


    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return row;
    }


    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return column;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPosition that = (ChessPosition) o;
        return row == that.row && column == that.column;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, column);
    }

    @Override
    public String toString() {
        return String.format("%s%d", (char) (column + COLUMN_CHAR_OFFSET), row);
    }

}
