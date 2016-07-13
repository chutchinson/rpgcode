/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode.ir;

import net.rpgtoolkit.rpgcode.ir.expressions.*;

import net.rpgtoolkit.rpgcode.ir.statements.*;

public interface NodeVisitor {

  void visit(CompilationUnit node);
  void visit(Parameter node);
  void visit(ClassDeclaration node);
  void visit(ClassFieldDeclaration node);
  void visit(FunctionDeclaration node);
  void visit(Block node);

  // statements

  void visit(ReturnStatement node);
  void visit(ConditionalStatement node);
  void visit(ExpressionStatement node);
  void visit(LoopStatement node);
  void visit(ForLoopStatement node);
  void visit(FlowControlStatement node);
  void visit(LabelStatement node);

  // expressions

  void visit(Identifier node);
  void visit(UnaryExpression node);
  void visit(CallExpression node);
  void visit(ConstantNumberExpression node);
  void visit(ConstantStringExpression node);
  void visit(ConstantBooleanExpression node);
  void visit(AdditiveBinaryExpression node);
  void visit(MultiplicativeBinaryExpression node);
  void visit(RelationalBinaryExpression node);
  void visit(ShiftBinaryExpression node);
  void visit(LogicalBinaryExpression node);
  void visit(BitwiseBinaryExpression node);
  void visit(AssignmentExpression node);

}
