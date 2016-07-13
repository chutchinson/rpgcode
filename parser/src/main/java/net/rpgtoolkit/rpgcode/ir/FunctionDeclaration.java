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

public class FunctionDeclaration implements Node {

  private Identifier name;
  private List<Parameter> parameters;
  private Block body;
  private Visibility visibility;
  private boolean inline;
  private boolean virtual;
  private boolean pure;

  public FunctionDeclaration(Identifier name) {
    if (name == null)
      throw new IllegalArgumentException();
    this.name = name;
    this.parameters = new ArrayList<>();
    this.visibility = Visibility.PUBLIC;
    this.inline = false;
    this.pure = false;
  }

  public boolean getIsInline() {
    return this.inline;
  }

  public void setIsInline(boolean value) {
    this.inline = value;
  }

  public boolean getIsAbstract(boolean value) {
    return this.pure;
  }

  public void setIsAbstract(boolean value) {
    this.pure = value;
  }

  public Visibility getVisibility() {
    return this.visibility;
  }

  public void setVisibility(Visibility visibility) {
    if (visibility == null)
      throw new IllegalArgumentException();
    this.visibility = visibility;
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

  public Block getBody() {
    return this.body;
  }

  public void setBody(Block block) {
    this.body = block;
  }

  @Override
  public void accept(NodeVisitor visitor) {
    visitor.visit(this);
  }

}
