/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

/**
 * A lexical token and associated contextual information.
 *
 * @author Chris Hutchinson
 */
public class Token {

  public TokenKind kind;
  public int tag;
  public int offset;
  public int length;
  public int line;
  public int column;

  public Token() {
    this.reset();
  }

  public final void reset() {
    this.kind = TokenKind.UNKNOWN;
    this.tag = 0;
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
