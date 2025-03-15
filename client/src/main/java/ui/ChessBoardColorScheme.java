package ui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ChessBoardColorScheme {

    public enum ColorType {
        BORDER, BORDER_TEXT, DARK_SQUARE, LIGHT_SQUARE, MOVE_MADE, HIGHLIGHT_MOVES_DARK, HIGHLIGHT_MOVES_LIGHT,
        WHITE_PIECE, BLACK_PIECE
    }

    public static final List<ChessBoardColorScheme> COLOR_SCHEMES = new ArrayList<>(
            List.of(new ChessBoardColorScheme(37, 0.46f, 0.6f, 0.12f, 0.39f, 0.495f, 0.88f, 0.12f),
                    new ChessBoardColorScheme(208, 0.56f, 0.52f, 0.15f, 0.43f, 0.55f, 0.88f, 0.14f),
                    new ChessBoardColorScheme(265, 0.6f, 0.5f, 0.15f, 0.55f, 0.64f, 0.88f, 0.12f),
                    new ChessBoardColorScheme(300, 0.5f, 0.54f, 0.15f, 0.43f, 0.57f, 0.9f, 0.11f),
                    new ChessBoardColorScheme(0, 0.52f, 0.5f, 0.15f, 0.41f, 0.54f, 0.9f, 0.11f),
                    new ChessBoardColorScheme(6, 0.5f, 0.68f, 0.28f, 0.58f, 0.72f, 0.92f, 0.15f)));

    private final float blackPiece;

    private final float border;

    private final float borderText;

    private final float darkSquare;

    private final float hue;

    private final float lightSquare;

    private final float saturation;

    private final float whitePiece;


    public ChessBoardColorScheme(float hue, float saturation, float border, float borderText, float darkSquare,
                                 float lightSquare, float whitePiece, float blackPiece) {
        this.hue = hue;
        this.saturation = saturation;
        this.border = border;
        this.borderText = borderText;
        this.darkSquare = darkSquare;
        this.lightSquare = lightSquare;
        this.whitePiece = whitePiece;
        this.blackPiece = blackPiece;
    }


    public String getColorEscapeSequence(ColorType type) {
        Color color = switch (type) {
            case BORDER -> getColor(((hue + 180) % 360), saturation, border);
            case BORDER_TEXT -> getColor(hue, saturation, borderText);
            case DARK_SQUARE -> getColor(hue, saturation, darkSquare);
            case LIGHT_SQUARE -> getColor(hue, saturation, lightSquare);
            case MOVE_MADE -> getColor(((hue + 270) % 360), saturation, lightSquare);
            case HIGHLIGHT_MOVES_DARK -> getColor(((hue + 90) % 360), saturation, darkSquare);
            case HIGHLIGHT_MOVES_LIGHT -> getColor(((hue + 90) % 360), saturation, lightSquare);
            case WHITE_PIECE -> getColor(((hue + 180) % 360), saturation, whitePiece);
            case BLACK_PIECE -> getColor(((hue + 180) % 360), saturation, blackPiece);
        };

        boolean text = type == ColorType.BORDER_TEXT || type == ColorType.BLACK_PIECE || type == ColorType.WHITE_PIECE;
        return setColor(text, color.getRed(), color.getGreen(), color.getBlue());
    }


    private static Color getColor(float hue, float saturation, float lightness) {
        float chroma = (1 - Math.abs(2 * lightness - 1)) * saturation;
        float lightCorrect = lightness - (chroma / 2);
        float range = hue / 60;

        float primary = chroma + lightCorrect;
        float secondary = chroma * (1 - Math.abs(range % 2 - 1)) + lightCorrect;

        return switch ((int) Math.floor(range)) {
            case 0 -> new Color(primary, secondary, lightCorrect);
            case 1 -> new Color(secondary, primary, lightCorrect);
            case 2 -> new Color(lightCorrect, primary, secondary);
            case 3 -> new Color(lightCorrect, secondary, primary);
            case 4 -> new Color(secondary, lightCorrect, primary);
            case 5 -> new Color(primary, lightCorrect, secondary);
            default -> throw new IllegalStateException("Unexpected value: " + (int) Math.floor(range));
        };

    }

    /**
     * Creates an escape sequence string that, if printed, should set the console text or background to any RGB color
     *
     * @param text  true if setting text color, false if setting background color
     * @param red   amount of red 0-255
     * @param green amount of green 0-255
     * @param blue  amount of blue 0-255
     * @return an escape sequence string that would set the console text or background to the specified color
     */
    private static String setColor(boolean text, int red, int green, int blue) {
        if (red < 0 || red > 255 || green < 0 || green > 255 || blue < 0 || blue > 255) {
            throw new IllegalArgumentException("Colors must be between 0 - 255");
        }
        return String.format("\u001B[%s8;2;%d;%d;%dm", (text) ? "3" : "4", red, green, blue);
    }


}
