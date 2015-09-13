package net.rpgtoolkit.rpgcode;

/**context
 * Encapsulates the active context of a lexical analysis. Intended use is to
 * store or force a lexer into a particular state.
 *
 * Not thread-safe.
 *
 * @author Chris Hutchinson
 */
public class LexerContext {
    
    public int length;
    public int ch;
    public int offset;
    public int column;
    public int line;
    
    public LexerContext() {
        this.length = 0;
        this.ch = 0;
        this.offset = 0;
        this.column = 0;
        this.line = 0;
    }
    
}
