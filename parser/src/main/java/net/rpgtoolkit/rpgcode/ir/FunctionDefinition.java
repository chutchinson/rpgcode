/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode.ir;

import java.util.ArrayList;
import java.util.List;

public class FunctionDefinition extends Node {

  private Identifier name;
  private List<Parameter> parameters;

  public FunctionDefinition(Identifier name) {
    if (name == null)
      throw new IllegalArgumentException();
    this.name = name;
    this.parameters = new ArrayList<>();
  }

  public Identifier getName() {
    return this.name;
  }

  public void setName(Identifier value) {
    if (value == null)
      throw new IllegalArgumentException();
    this.name = value;
  }

  public List<Parameter> getParameters() {
    return this.parameters;
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
