package chess.moves.piece;

public class QueenMovesCalculator extends LineMovesCalculator {
    private static final int[][] QUEEN_DIRECTIONS =
            new int[][]{{1, 1}, {1, 0}, {1, -1}, {0, 1}, {0, -1}, {-1, 1}, {-1, 0}, {-1, -1}};

    public QueenMovesCalculator() {
        super(QUEEN_DIRECTIONS);
    }
}