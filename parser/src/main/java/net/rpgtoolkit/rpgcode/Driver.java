package net.rpgtoolkit.rpgcode;

/// NOTE: This class should be removed and replaced with unit testing.

public class Driver {

    public static void main(String[] args) {

        Lexer lexer = new Lexer("function main() { return 0 <> 4; } // comment");
        Token token = lexer.scan();

        while (token.kind != TokenKind.END) {
            System.out.println(String.format("%s %s",
                    token, lexer.lexeme(token)));
            token = lexer.scan();
        }


    }

}
