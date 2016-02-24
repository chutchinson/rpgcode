/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

import net.rpgtoolkit.rpgcode.ir.*;

import java.util.HashSet;
import java.util.Set;

/**
 * RPGCode reference parser
 */
public class Parser {

  private final Lexer lexer;
  private Token token;
  private Set<ParserListener> listeners;

  public Parser(Lexer lexer) {
    if (lexer == null) {
      throw new NullPointerException();
    }
    this.lexer = lexer;
    this.listeners = new HashSet<>();
  }

  public void addListener(ParserListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(ParserListener listener) {
    this.listeners.remove(listener);
  }

  public Lexer getLexer() {
    return this.lexer;
  }

  public Parameter parseParameter() {

    if (!expect(TokenKind.IDENTIFIER)) return null;

    final Identifier name = Identifier.valueOf(lexeme());
    final Parameter node = new Parameter(name);

    return node;

  }

  public FunctionDefinition parseFunctionDefinition() {

    // Parse function keyword

    if (!expect(TokenKind.IDENTIFIER, "function")) return null;

    // Parse function name and create a function definition

    read();
    if (!expect(TokenKind.IDENTIFIER)) return null;

    final String name = lexeme();
    final FunctionDefinition node = new FunctionDefinition(
      Identifier.valueOf(name));

    read();
    if (!expect(TokenKind.PAREN_LEFT)) return null;

    // Parse parameters

    read();
    while (token.kind != TokenKind.PAREN_RIGHT) {

      final Parameter param = parseParameter();
      if (param != null)
        node.getParameters().add(param);

      read();
      if (token.kind == TokenKind.COMMA) {
        read();
      }

    }

    if (!expect(TokenKind.PAREN_RIGHT)) return null;

    return node;

  }

  public void read() {
    this.token = this.lexer.scan();
  }

  public void error(String message) {
    final int line = (token != null) ? token.line : 0;
    final int column = (token != null) ? token.column : 0;
    final ParseError err = new ParseError(line, column, message);
    for (final ParserListener listener : listeners) {
      listener.error(this, err);
    }
  }

  private boolean expect(TokenKind kind) {
    if (token != null) {
      if (token.kind == kind) {
        return true;
      }
      else {
        final String message = String.format("expected %s but found %s", kind, token.kind);
        error(message);
      }
    }
    return false;
  }

  private boolean expect(TokenKind kind, String value) {
    if (value != null && expect(kind)) {
      return value.equals(lexeme());
    }
    return false;
  }

  private String lexeme() {
    return (token != null) ? lexer.lexeme(token) : null;
  }

}
