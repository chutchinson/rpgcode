/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

import java.util.HashMap;
import java.util.Map;

/**
 * Lexical analyzer (lexer) for the RPGCode grammar.
 *
 * @author Chris Hutchinson
 */
public class Lexer {

  private final LexerContext context;
  private final Map<String, Integer> keywords;
  private String input;

  public Lexer(String input) {
    this(input, new LexerContext());
  }

  public Lexer(String input, LexerContext context) {
    if (input == null || context == null) {
      throw new NullPointerException();
    }
    this.input = input;
    this.context = context;
    this.context.length = input.length();
    this.keywords = new HashMap<>();
    this.keywords.put("function", Keywords.FUNCTION);
    this.keywords.put("method", Keywords.FUNCTION);
  }

  public String lexeme(Token token) {
    return this.input.substring(token.offset, token.offset + token.length);
  }

  public Token scan() {

    skip();

    final Token token = new Token();

    token.kind = TokenKind.UNKNOWN;
    token.offset = this.context.offset;
    token.line = this.context.line;
    token.column = this.context.column;

    lookahead();

    switch (this.context.ch) {
      case '\0':
        accept();
        token.kind = TokenKind.END;
        break;
      case '\n':
        accept();
        this.context.line++;
        this.context.column = 0;
        token.kind = TokenKind.EOL;
        break;
      case ';':
        accept();
        token.kind = TokenKind.SEMICOLON;
        break;
      case '!':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.NOT_EQUALS;
            break;
          default:
            token.kind = TokenKind.NOT;
            break;
        }
        break;
      case ',':
        accept();
        token.kind = TokenKind.COMMA;
        break;
      case ':':
        accept();
        token.kind = TokenKind.COLON;
        break;
      case '.':
        accept();
        token.kind = TokenKind.DOT;
        break;
      case '(':
        accept();
        token.kind = TokenKind.PAREN_LEFT;
        break;
      case ')':
        accept();
        token.kind = TokenKind.PAREN_RIGHT;
        break;
      case '{':
        accept();
        token.kind = TokenKind.BRACE_LEFT;
        break;
      case '}':
        accept();
        token.kind = TokenKind.BRACE_RIGHT;
        break;
      case '[':
        accept();
        token.kind = TokenKind.BRACKET_LEFT;
        break;
      case ']':
        accept();
        token.kind = TokenKind.BRACKET_RIGHT;
        break;
      case '<':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.LESS_THAN_OR_EQUAL_TO;
            break;
          case '<':
            accept();
            lookahead();
            switch (this.context.ch) {
              case '=':
                accept();
                token.kind = TokenKind.ASSIGN_SHIFT_LEFT;
                break;
              default:
                token.kind = TokenKind.SHIFT_LEFT;
                break;
            }
            break;
          case '>':
            accept();
            token.kind = TokenKind.NOT_EQUALS;
            break;
          default:
            token.kind = TokenKind.LESS_THAN;
            break;
        }
        break;
      case '>':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.GREATER_THAN_OR_EQUAL_TO;
            break;
          case '>':
            accept();
            lookahead();
            switch (this.context.ch) {
              case '=':
                accept();
                token.kind = TokenKind.ASSIGN_SHIFT_RIGHT;
                break;
              default:
                token.kind = TokenKind.SHIFT_RIGHT;
                break;
            }
            break;
          default:
            token.kind = TokenKind.GREATER_THAN;
            break;
        }
        break;
      case '=':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.EQUALS;
            break;
          default:
            token.kind = TokenKind.ASSIGN;
            break;
        }
        break;
      case '+':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '+':
            accept();
            token.kind = TokenKind.INCREMENT;
            break;
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_PLUS;
            break;
          default:
            token.kind = TokenKind.PLUS;
            break;
        }
        break;
      case '-':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '-':
            accept();
            token.kind = TokenKind.DECREMENT;
            break;
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_MINUS;
            break;
          default:
            token.kind = TokenKind.MINUS;
            break;
        }
        break;
      case '*':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_MULTIPLY;
            break;
          default:
            token.kind = TokenKind.MULTIPLY;
            break;
        }
        break;
      case '/':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '/':
            this.comment(token);
            break;
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_DIVIDE;
            break;
          default:
            token.kind = TokenKind.DIVIDE;
            break;
        }
        break;
      case '%':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_MODULUS;
            break;
          default:
            token.kind = TokenKind.MODULUS;
            break;
        }
        break;
      case '^':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_POW;
            break;
          default:
            token.kind = TokenKind.POW;
            break;
        }
        break;
      case '|':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '|':
            accept();
            token.kind = TokenKind.OR_LOGICAL;
            break;
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_OR;
            break;
          default:
            token.kind = TokenKind.OR_BINARY;
            break;
        }
        break;
      case '&':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '&':
            accept();
            token.kind = TokenKind.AND_LOGICAL;
            break;
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_AND;
            break;
          default:
            token.kind = TokenKind.AND_BINARY;
            break;
        }
        break;
      case '`':
        accept();
        lookahead();
        switch (this.context.ch) {
          case '=':
            accept();
            token.kind = TokenKind.ASSIGN_XOR;
            break;
          default:
            token.kind = TokenKind.XOR;
            break;
        }
        break;
      case '"':
        this.string(token);
        break;
      default:
        if (isIdentifier(this.context.ch)) {
          this.identifier(token);
        } else if (isDigit(this.context.ch)) {
          this.number(token);
        } else {
          accept();
        }
        break;
    }

    // compute token length

    token.length = this.context.offset - token.offset;

    return token;

  }

  public void accept() {
    this.context.offset++;
    this.context.column++;
  }

  public void lookahead() {
    if (this.context.offset < this.context.length) {
      this.context.ch = this.input.charAt(this.context.offset);
    } else {
      this.context.ch = 0;
    }
  }

  public void skip() {
    lookahead();
    while (isWhitespace(this.context.ch)) {
      accept();
      lookahead();
    }
  }

  public void string(final Token token) {
    token.kind = TokenKind.STRING;
    accept();
    lookahead();
    while (this.context.ch != '"' && this.context.ch != 0) {
      accept();
      lookahead();
    }
    accept();
  }

  public void comment(final Token token) {
    token.kind = TokenKind.COMMENT;
    accept();
    lookahead();
    while (this.context.ch != '\n' && this.context.ch != 0) {
      accept();
      lookahead();
    }
  }

  public void number(final Token token) {
    token.kind = TokenKind.NUMBER;
    lookahead();
    while (isDigit(this.context.ch)) {
      accept();
      lookahead();
    }
  }

  public void identifier(final Token token) {
    token.kind = TokenKind.IDENTIFIER;
    lookahead();
    while (isIdentifier(this.context.ch)) {
      accept();
      lookahead();
    }
  }

  protected boolean isDigit(int ch) {
    return (ch >= '0' && ch <= '9');
  }

  protected boolean isWhitespace(int ch) {
    return (ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n');
  }

  protected boolean isIdentifier(int ch) {
    return (ch >= 'a' && ch <= 'z')
      || (ch >= 'A' && ch <= 'Z')
      || (ch == '_') || (ch == '!') || (ch == '$');
  }

}
