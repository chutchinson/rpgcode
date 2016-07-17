package net.rpgtoolkit.rpgcode;

public class ParseError {

    private final int line;
    private final int column;
    private final String message;

    public static ParseError build(Token token, String message) {
        return new ParseError(token.line, token.column, message);
    }

    public ParseError(int line, int column, String msg) {
        this.line = line;
        this.column = column;
        this.message = msg;
    }

    public int getLine() {
        return this.line;
    }

    public int getColumn() {
        return this.column;
    }

    public String getMessage() {
        return this.message;
    }

}
