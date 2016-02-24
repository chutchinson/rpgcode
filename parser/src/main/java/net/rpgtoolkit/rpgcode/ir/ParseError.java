/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode.ir;

public class ParseError {

  private final int line;
  private final int column;
  private final String message;

  public ParseError(int line, int column, String message) {
    if (message == null)
      throw new IllegalArgumentException();
    this.line = line;
    this.column = column;
    this.message = message;
  }

  public String getMessage() {
    return this.message;
  }

  public int getLine() {
    return this.line;
  }

  public int getColumn() {
    return this.column;
  }

  @Override
  public String toString() {
    return String.format("(%04d, %04d) %s", this.line, this.column, this.message);
  }

}
