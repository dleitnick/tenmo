package com.techelevator.util;

public class ColorUtil {

    private final String BLOCK = "█";
    private int paddingHorizontal;
    private int paddingVertical;
    private boolean hasInternalVerticalPadding;

    public ColorUtil(int paddingHorizontal, int paddingVertical, boolean hasInternalVerticalPadding) {
        this.paddingHorizontal = paddingHorizontal;
        this.paddingVertical = paddingVertical;
        this.hasInternalVerticalPadding = hasInternalVerticalPadding;
    }
    public ColorUtil(int paddingHorizontal, int paddingVertical) {
        this(paddingHorizontal, paddingVertical, true);
    }

    public ColorUtil() {
        this(8, 2, true);
    }

    // TODO: Refactor with BigDecimal to avoid some issue with smaller color range
    // TODO: Add inside padding - kinda done
    // TODO: Reword method instructions
    public void print(String title) {
        final int RED = 133;
        final int GREEN = 187;
        final int BLUE = 101;
        final char COLOR = 'r';
        String[] titleLines = title.split("\n");
        String[] titleLinesWithPadding;
        if (hasInternalVerticalPadding) {
            titleLinesWithPadding = new String[titleLines.length + 2];
            titleLinesWithPadding[0] = "";
            titleLinesWithPadding[titleLinesWithPadding.length - 1] = "";
            for (int i = 1; i < titleLinesWithPadding.length - 1; i++) {
                titleLinesWithPadding[i] = titleLines[i - 1];
            }
            // Make title all uppercase
            StringBuilder titleBuilder = new StringBuilder(titleLines[0].toUpperCase());
            // Add spaces between each character
            for (int i = 0; i <= titleBuilder.length(); i += 2) {
                titleBuilder.insert(i, " ");
            }
            int longestLength = 0;
            for (String s : titleLinesWithPadding) {
                if (s.length() > longestLength) {
                    longestLength = s.length();
                }
            }
            String format = "%-" + longestLength + "s";
            for (int i = 0; i < titleLinesWithPadding.length; i++) {
                titleLinesWithPadding[i] = String.format(format, titleLinesWithPadding[i]);
            }
            title = String.format(format, titleBuilder.toString());
        } else {
            titleLinesWithPadding = titleLines;
            titleLinesWithPadding[0] += " ";
        }

        for (int i = 0; i < paddingVertical; i++) {
            System.out.println(makeBlockGradient(title, RED, GREEN, BLUE, 'r'));
        }

        String formatString = " %-" + (title.length() - 1) + "s";
        for (int i = 0; i < titleLinesWithPadding.length; i++) {
            System.out.print(makeBlockGradient(title, RED, GREEN, BLUE, COLOR, 150, 'l'));
            if (i == 1) System.out.print(stringColor(title, 255, 255, 255, "bold,italic"));
            else System.out.printf(formatString, titleLinesWithPadding[i]);
            System.out.println(makeBlockGradient(title, RED, GREEN, BLUE, COLOR, 150, 'r'));
        }

        for (int i = 0; i < paddingVertical; i++) {
            System.out.println(makeBlockGradient(title, RED, GREEN, BLUE, COLOR));
        }

//        System.out.println();
    }

    /**
     * Create block gradient that extends to the size of the text with padding
     * @param title - text to use
     * @param red - (0-255) initial state of color
     * @param green - (0-255) initial state of color
     * @param blue - (0-255) initial state of color
     * @param colorChanging - 'r', 'g', OR 'b' - the color that will be added in gradient
     * @param endOfColor - (0-255) final state of color
     * @param sides - 'l' OR 'r' - use for padding directly on left or right of text
     * @return a string made of █ that has the specified color gradient
     */
    private String makeBlockGradient(String title, int red, int green, int blue, char colorChanging, int endOfColor, char sides) {
        StringBuilder str = new StringBuilder();
        if (sides == 'r') str.append(backgroundColor(32, 32, 32)).append("  ");
        double colorChoice;
        if (colorChanging == 'r') colorChoice = red;
        else if (colorChanging == 'g') colorChoice = green;
        else colorChoice = blue;
        double blockItr = title.length() + paddingHorizontal * 2;
        for (int i = 0; i < paddingHorizontal; i++) {
            str.append(backgroundColor(red, green, blue)).append(" ");
        }
//        if (sides == 'l') {
//            endOfColor = (int) colorChoice;
//        }
//        else if (sides == 'r') {
//            colorChoice = colorChoice;
//        }
//        if (sides == 'r') str.append(backgroundColor(32, 32, 32)).append("  ");
//        for (double i = colorChoice; Math.round(i) < endOfColor; i += blockItr) {
//            if (colorChanging == 'r') red = (int) i;
//            else if (colorChanging == 'g') green = (int) i;
//            else blue = (int) i;
////            str.append(stringColor(BLOCK, red, green, blue)); // if using blocks instead of smooth gradient
//            str.append(backgroundColor(red, green, blue)).append(" ");
//        }
        if (sides == 'l') str.append(backgroundColor(32, 32, 32)).append("  ");
        else str.append("\u001b[0m");
        return str.toString();
    }


    /**
     * Create block gradient that extends to the size of the text with padding
     * @param title - text to use
     * @param red - (0-255) initial state of color
     * @param green - (0-255) initial state of color
     * @param blue - (0-255) initial state of color
     * @param colorChanging - 'r', 'g', OR 'b' - the color that will be added in gradient
     * @param endOfColor - (0-255) final state of color
     * @return a string made of █ that has the specified color gradient
     */
    private String makeBlockGradient(String title, int red, int green, int blue, char colorChanging, int endOfColor) {
        return makeBlockGradient(title, red, green, blue, colorChanging, 255, 'n');
    }

    /**
     * Create block gradient that extends to the size of the text with padding
     * @param title - text to use
     * @param red - (0-255) initial state of color
     * @param green - (0-255) initial state of color
     * @param blue - (0-255) initial state of color
     * @param colorChanging - 'r', 'g', OR 'b' - the color that will be added in gradient
     * @return a string made of █ that has the specified color gradient
     */
    private String makeBlockGradient(String title, int red, int green, int blue, char colorChanging) {
        return makeBlockGradient(title, red, green, blue, colorChanging, 255, 'n');
    }
    // TODO: Combine all string methods into a more modular style
    public String stringStyle(String str, String style) {
        StringBuilder styler = new StringBuilder();
        String[] styles = style.split(",");
        for (String styletype : styles) {
            switch (styletype) {
                case "italic":
                    styler.append(";3");
                    break;
                case "underline":
                    styler.append(";4");
                    break;
                case "bold":
                    styler.append(";1");
                    break;
            }
        }
        return String.format("\u001b[0%sm%s\u001b[0m", styler, str);
    }

    public String stringColor(String str, int red, int green, int blue) {
        return String.format("\u001b[38;2;%s;%s;%sm%s\u001b[0m", red, green, blue, str);
    }

    public String stringColor(String str, int red, int green, int blue, String style) {
        StringBuilder styler = new StringBuilder();
        String[] styles = style.split(",");
        for (String styletype : styles) {
            switch (styletype) {
                case "italic":
                    styler.append(";3");
                    break;
                case "underline":
                    styler.append(";4");
                    break;
                case "bold":
                    styler.append(";1");
                    break;
            }
        }
        return String.format("\u001b[38;2;%s;%s;%s%sm%s\u001b[0m", red, green, blue, styler.toString(), str);
    }

    private String backgroundColor(int red, int green, int blue) {
        return String.format("\u001b[48;2;%s;%s;%sm", red, green, blue); // The 48 means background color
    }

}
