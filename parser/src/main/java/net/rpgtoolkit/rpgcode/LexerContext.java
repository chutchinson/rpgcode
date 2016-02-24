/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

/**
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
