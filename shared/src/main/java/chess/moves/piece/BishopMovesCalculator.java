package chess.moves.piece;

public class BishopMovesCalculator extends LineMovesCalculator {
    private static final int[][] BISHOP_DIRECTIONS = new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};

    public BishopMovesCalculator() {
        super(BISHOP_DIRECTIONS);
    }
}