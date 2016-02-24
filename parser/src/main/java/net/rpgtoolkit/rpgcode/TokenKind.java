/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

/**
 * Enumeration of possible token types that can be derived
 * from a given input as a result of lexical analysis.
 *
 * @author Chris Hutchinson
 */
public enum TokenKind {

  UNKNOWN,
  END,
  EOL,

  DOT,
  PLUS,
  MINUS,
  MULTIPLY,
  DIVIDE,
  MODULUS,
  POW,
  INCREMENT,
  DECREMENT,
  SHIFT_LEFT,
  SHIFT_RIGHT,
  GREATER_THAN,
  GREATER_THAN_OR_EQUAL_TO,
  LESS_THAN,
  LESS_THAN_OR_EQUAL_TO,
  EQUALS,
  NOT_EQUALS,
  NOT,
  AND_LOGICAL,
  AND_BINARY,
  OR_LOGICAL,
  OR_BINARY,
  XOR,
  ASSIGN,
  ASSIGN_PLUS,
  ASSIGN_MINUS,
  ASSIGN_MULTIPLY,
  ASSIGN_DIVIDE,
  ASSIGN_MODULUS,
  ASSIGN_POW,
  ASSIGN_OR,
  ASSIGN_AND,
  ASSIGN_XOR,
  ASSIGN_SHIFT_LEFT,
  ASSIGN_SHIFT_RIGHT,

  COLON,
  PAREN_LEFT,
  PAREN_RIGHT,
  BRACKET_LEFT,
  BRACKET_RIGHT,
  BRACE_LEFT,
  BRACE_RIGHT,

  COMMENT,
  IDENTIFIER,
  STRING,
  NUMBER

}
