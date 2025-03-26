package ui;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class BoardPrinter {
    private final ChessGame.TeamColor perspective;
    private ChessBoardColorScheme colorScheme;
    private ChessGame currentGame = new ChessGame();
    private boolean usePieceChars = true;

    public BoardPrinter(ChessGame.TeamColor perspective) {
        this.perspective = perspective;
        setColorScheme(ChessBoardColorScheme.COLOR_SCHEMES.getFirst());
    }

    public void setCurrentGame(ChessGame currentGame) {
        this.currentGame = currentGame;
    }

    public void setColorScheme(ChessBoardColorScheme colorScheme) {
        this.colorScheme = colorScheme;
    }

    public void toggleUsePieceChars() {
        usePieceChars = !usePieceChars;
    }

    public void printGame() {
        printGame(new HashSet<>(), new HashSet<>());
    }

    public void printNewGame(ChessGame newGame) {
        Collection<ChessPosition> differences = determineDifferences(newGame, currentGame);
        setCurrentGame(newGame);
        printGame(new HashSet<>(), differences);
    }

    public void highlightMoves(ChessPosition position) {
        if(currentGame.getBoard().getPiece(position) == null) {
            throw new IllegalArgumentException("No piece at position " + position);
        }
        Set<ChessPosition> highlight =
                currentGame.validMoves(position).stream().map(ChessMove::getEndPosition).collect(Collectors.toSet());
        printGame(highlight, Set.of(position));
    }

    private void printGame(Collection<ChessPosition> highlight, Collection<ChessPosition> differences) {
        if(currentGame == null) {
            throw new IllegalStateException("Current game has not yet been received");
        }
        System.out.println();
        printHeader(colorScheme, perspective);

        for (int i = 1; i <= 8; i++) {
            int row = (perspective == ChessGame.TeamColor.WHITE) ? 9 - i : i;

            System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER_TEXT));
            System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER));
            System.out.printf(" %d ", row);

            for (int j = 1; j <= 8; j++) {
                int col = (perspective == ChessGame.TeamColor.BLACK) ? 9 - j : j;

                ChessPosition pos = new ChessPosition(row, col);

                ChessBoardColorScheme.ColorType backgroundType = getBackgroundType(pos, highlight, differences);

                System.out.print(colorScheme.getColorEscapeSequence(backgroundType));

                ChessPiece piece = currentGame.getBoard().getPiece(pos);
                if (piece == null) {
                    System.out.print(usePieceChars ? EscapeSequences.EMPTY : "\u2009   \u2009");
                }
                else {
                    System.out.print(switch (piece.getTeamColor()) {
                        case WHITE -> colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.WHITE_PIECE);
                        case BLACK -> colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BLACK_PIECE);
                    });
                    System.out.print(getPieceChar(piece));
                }
            }

            System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER_TEXT));
            System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER));
            System.out.printf(" %d ", row);
            System.out.print(EscapeSequences.RESET_BG_COLOR);
            System.out.print(EscapeSequences.RESET_TEXT_COLOR);
            System.out.println();
        }


        printHeader(colorScheme, perspective);
        System.out.println();
    }

    private ChessBoardColorScheme.ColorType getBackgroundType(ChessPosition pos,
                                                                     Collection<ChessPosition> highlight,
                                                                     Collection<ChessPosition> differences) {
        boolean lightSquare = (pos.getRow() + pos.getColumn()) % 2 == 1;

        if (highlight.contains(pos)) {
            return (lightSquare) ? ChessBoardColorScheme.ColorType.HIGHLIGHT_MOVES_LIGHT :
                    ChessBoardColorScheme.ColorType.HIGHLIGHT_MOVES_DARK;
        }
        else if (differences.contains(pos)) {
            return ChessBoardColorScheme.ColorType.MOVE_MADE;
        }
        else {
            return (lightSquare) ? ChessBoardColorScheme.ColorType.LIGHT_SQUARE :
                    ChessBoardColorScheme.ColorType.DARK_SQUARE;
        }
    }


    private void printHeader(ChessBoardColorScheme colorScheme, ChessGame.TeamColor perspective) {
        System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER_TEXT));
        System.out.print(colorScheme.getColorEscapeSequence(ChessBoardColorScheme.ColorType.BORDER));
        System.out.print("   ");

        if (perspective == ChessGame.TeamColor.BLACK) {
            for (char c = 'h'; c >= 'a'; c--) {
                System.out.printf("\u2009 %s \u2009", c);
            }
        } else {
            for (char c = 'a'; c <= 'h'; c++) {
                System.out.printf("\u2009 %s \u2009", c);
            }
        }

        System.out.print("   ");

        System.out.print(EscapeSequences.RESET_TEXT_COLOR);
        System.out.print(EscapeSequences.RESET_BG_COLOR);
        System.out.println();
    }

    private Collection<ChessPosition> determineDifferences(ChessGame newGame, ChessGame baseGame) {
        Collection<ChessPosition> differences = new HashSet<>();
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                ChessPosition pos = new ChessPosition(i, j);
                ChessPiece piece = baseGame.getBoard().getPiece(pos);
                ChessPiece prevPiece = newGame.getBoard().getPiece(pos);

                if (!Objects.equals(piece, prevPiece)) {
                    differences.add(pos);
                }
            }
        }
        return differences;
    }


    private String getPieceChar(ChessPiece piece) {
        if(usePieceChars) {
            return switch (piece.getPieceType()) {
                case KING -> EscapeSequences.BLACK_KING;
                case QUEEN -> EscapeSequences.BLACK_QUEEN;
                case BISHOP -> EscapeSequences.BLACK_BISHOP;
                case KNIGHT -> EscapeSequences.BLACK_KNIGHT;
                case ROOK -> EscapeSequences.BLACK_ROOK;
                case PAWN -> EscapeSequences.BLACK_PAWN;
            };
        }
        else {
            String base = switch (piece.getPieceType()) {
                case KING -> "\u2009 k \u2009";
                case QUEEN -> "\u2009 q \u2009";
                case BISHOP -> "\u2009 b \u2009";
                case KNIGHT -> "\u2009 n \u2009";
                case ROOK -> "\u2009 r \u2009";
                case PAWN -> "\u2009 p \u2009";
            };
            return piece.getTeamColor() == ChessGame.TeamColor.WHITE ? base.toUpperCase() : base.toLowerCase();
        }
    }
}
