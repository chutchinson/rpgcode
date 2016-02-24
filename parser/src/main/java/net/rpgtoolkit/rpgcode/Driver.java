/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
