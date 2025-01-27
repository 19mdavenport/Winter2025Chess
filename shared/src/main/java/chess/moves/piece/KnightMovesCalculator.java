package chess.moves.piece;

public class KnightMovesCalculator extends DiscreteMoveCalculator {
    private static final int[][] KNIGHT_DIRECTIONS =
            new int[][]{{1, 2}, {2, 1}, {1, -2}, {2, -1}, {-1, 2}, {-2, 1}, {-1, -2}, {-2, -1}};

    public KnightMovesCalculator() {
        super(KNIGHT_DIRECTIONS);
    }
}
