package net.rpgtoolkit.rpgcode;

/**
 * A lexical token and associated contextual information.
 *
 * @author Chris Hutchinson
 */
public class Token {

    public TokenKind kind;
    public int offset;
    public int length;
    public int line;
    public int column;

    public Token() {
        this.reset();
    }

    public final void reset() {
        this.kind = TokenKind.UNKNOWN;
        this.offset = 0;
        this.length = 0;
        this.line = 0;
        this.column = 0;
    }

    @Override
    public String toString() {
        return String.format("(%04d, %04d) %s", 
                this.line, this.column, this.kind);
    }
    
}
