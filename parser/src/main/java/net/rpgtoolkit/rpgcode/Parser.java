package net.rpgtoolkit.rpgcode;

/**
 * Rpgcode reference parser
 */
public class Parser {

    private final Lexer lexer;

    public Parser(Lexer lexer) {
        if (lexer == null) {
            throw new NullPointerException();
        }
        this.lexer = lexer;
    }

}
