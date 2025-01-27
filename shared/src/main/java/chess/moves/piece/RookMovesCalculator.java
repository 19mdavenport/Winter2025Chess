package chess.moves.piece;

public class RookMovesCalculator extends LineMovesCalculator {
    private static final int[][] ROOK_DIRECTIONS = new int[][]{{1, 0}, {0, 1}, {-1, 0}, {0, -1}};

    public RookMovesCalculator() {
        super(ROOK_DIRECTIONS);
    }
}