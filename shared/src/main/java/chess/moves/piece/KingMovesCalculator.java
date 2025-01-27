package chess.moves.piece;

public class KingMovesCalculator extends DiscreteMoveCalculator {
    private static final int[][] KING_DIRECTIONS =
            new int[][]{{1, 1}, {-1, 1}, {1, -1}, {-1, -1}, {0, 1}, {1, 0}, {-1, 0}, {0, -1}};

    public KingMovesCalculator() {
        super(KING_DIRECTIONS);
    }

}