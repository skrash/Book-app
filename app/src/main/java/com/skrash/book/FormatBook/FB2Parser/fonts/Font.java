package com.skrash.book.FormatBook.FB2Parser.fonts;

public class Font {

    protected final int startIndex, finishIndex;

    protected Font(String emphasis, String p) {
        startIndex = p.indexOf(emphasis);
        finishIndex = startIndex + emphasis.length();
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getFinishIndex() {
        return finishIndex;
    }
}
