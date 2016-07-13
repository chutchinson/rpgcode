/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

import net.rpgtoolkit.rpgcode.ir.*;
import net.rpgtoolkit.rpgcode.ir.expressions.*;
import net.rpgtoolkit.rpgcode.ir.statements.*;

import java.util.*;

/**
 * RPGCode reference parser
 */
public class Parser {

  private final Lexer lexer;
  private final LinkedList<Token> tokens;

  public Parser(Lexer lexer) {

    if (lexer == null) {
      throw new NullPointerException();
    }

    this.lexer = lexer;
    this.tokens = new LinkedList<>();

  }

  public CompilationUnit parse() {

    final CompilationUnit node = new CompilationUnit();
    final FunctionDeclaration topLevelFn = new FunctionDeclaration(new Identifier("$_start"));
    final Block topLevelFnBody = new Block();

    topLevelFn.setBody(topLevelFnBody);
    node.getFunctionDeclarations().add(topLevelFn);

    while (!match(TokenKind.END)) {

      if (match(TokenKind.COMMENT)) {
        accept();
        continue;
      }

      if (match(TokenKind.KEYWORD, Keywords.CLASS)) {
        final ClassDeclaration decl = parseClassDeclaration();
        if (decl != null)
          node.getClassDeclarations().add(decl);
      }
      else if (match(TokenKind.KEYWORD, Keywords.FUNCTION) || match(TokenKind.KEYWORD, Keywords.INLINE)) {
        final FunctionDeclaration decl = parseFunctionDeclaration();
        if (decl != null)
          node.getFunctionDeclarations().add(decl);
      }
      else if (matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
        accept();
      }
      else {
        final Statement stmt = parseStatement();
        if (stmt != null) {
          topLevelFn.getBody().getStatements().add(stmt);
        }
        else {
          error("unexpected token in program body");
          accept();
        }
      }

    }

    return node;

  }

  public ClassFieldDeclaration parseClassFieldDeclaration() {

    if (match(TokenKind.KEYWORD, Keywords.VAR)) {
      accept();
    }

    final Identifier name = parseIdentifier();
    final ClassFieldDeclaration node = new ClassFieldDeclaration(name);

    expectEndofStatement();

    return node;

  }

  public ClassDeclaration parseClassDeclaration() {

    expect(TokenKind.KEYWORD, Keywords.CLASS);

    final Identifier name = parseIdentifier();
    final ClassDeclaration node = new ClassDeclaration(name);

    // TODO: parse inheritance list

    skip();
    expect(TokenKind.BRACE_LEFT);

    Visibility currentVisibility = Visibility.PUBLIC;

    while (!matchAny(TokenKind.BRACE_RIGHT, TokenKind.END)) {

      if (match(TokenKind.KEYWORD, Keywords.PUBLIC)) {
        currentVisibility = parseVisibilityLabel();
      }
      else if (match(TokenKind.KEYWORD, Keywords.PRIVATE)) {
        currentVisibility = parseVisibilityLabel();
      }
      else if (match(TokenKind.KEYWORD, Keywords.PROTECTED)) {
        currentVisibility = parseVisibilityLabel();
      }
      else if (match(TokenKind.KEYWORD, Keywords.FUNCTION) || match(TokenKind.KEYWORD, Keywords.INLINE)) {
        final FunctionDeclaration fn = parseFunctionDeclaration();
        if (fn != null) {
          fn.setVisibility(currentVisibility);
          node.getFunctionDeclarations().add(fn);
        }
      }
      else if (match(TokenKind.KEYWORD, Keywords.VAR) || match(TokenKind.IDENTIFIER)) {
        final ClassFieldDeclaration field = parseClassFieldDeclaration();
        if (field != null) {
          field.setVisibility(currentVisibility);
          node.getFieldDeclarations().add(field);
        }
      }
      else if (matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
        accept();
      }
      else {
        error("unexpected token in class declaration");
        accept();
      }

    }

    expect(TokenKind.BRACE_RIGHT);

    return node;

  }

  private Visibility parseVisibilityLabel() {

    Visibility result;

    final Token token = expect(TokenKind.KEYWORD);

    switch (token.tag) {
      case Keywords.PUBLIC:
        result = Visibility.PUBLIC;
        break;
      case Keywords.PRIVATE:
        result = Visibility.PRIVATE;
        break;
      case Keywords.PROTECTED:
        result = Visibility.PROTECTED;
        break;
      default:
        error("unrecognized visibility label");
        return Visibility.PRIVATE;
    }

    expect(TokenKind.COLON);
    expectEndofStatement();

    return result;

  }

  public FunctionDeclaration parseFunctionDeclaration() {

    boolean inline = false;

    if (match(TokenKind.KEYWORD, Keywords.INLINE)) {
      accept();
      inline = true;
    }

    expect(TokenKind.KEYWORD, Keywords.FUNCTION);

    final Identifier name = parseIdentifier();
    final FunctionDeclaration node = new FunctionDeclaration(name);

    node.setIsInline(inline);

    expect(TokenKind.PAREN_LEFT);

    while (!matchAny(TokenKind.PAREN_RIGHT, TokenKind.END)) {

      final Parameter param = parseParameter();
      if (param != null)
        node.getParameters().add(param);

      if (match(TokenKind.COMMA))
        accept();

    }

    expect(TokenKind.PAREN_RIGHT);

    if (match(TokenKind.EQUALS)) {
      accept();
      expect(TokenKind.NUMBER);
      expectEndofStatement();
      node.setIsAbstract(true);
    }
    else {
      skip();
      final Block block = parseBlock();
      node.setBody(block);
    }

    return node;

  }

  private Block parseBlock() {

    final Block node = new Block();

    expect(TokenKind.BRACE_LEFT);

    while (true) {

      skip();

      if (!matchAny(TokenKind.BRACE_RIGHT, TokenKind.END)) {
        final Statement stmt = parseStatement();
        if (stmt != null)
          node.getStatements().add(stmt);
      }
      else {
        break;
      }

    }

    expect(TokenKind.BRACE_RIGHT);

    return node;

  }

  public Statement parseStatement() {

    if (match(TokenKind.KEYWORD, Keywords.RETURN)) {
      return parseReturnStatement();
    }
    else if (match(TokenKind.KEYWORD, Keywords.IF)) {
      return parseConditionalStatement();
    }
    else if (match(TokenKind.KEYWORD, Keywords.DO)) {
      return parseDoWhileStatement();
    }
    else if (match(TokenKind.KEYWORD, Keywords.WHILE)) {
      return parseWhileStatement();
    }
    else if (match(TokenKind.KEYWORD, Keywords.UNTIL)) {
      return parseUntilStatement();
    }
    else if (match(TokenKind.KEYWORD, Keywords.FOR)) {
      return parseForStatement();
    }
    else if (matchAll(TokenKind.IDENTIFIER, TokenKind.COLON)) {
      return parseLabelStatement();
    }
    else {
      final Expression expr = parseExpression();
      expectEndofStatement();
      return new ExpressionStatement(expr);
    }

  }

  public ForLoopStatement parseForStatement() {

    Expression initial = null;
    Expression condition = null;
    Expression post = null;

    expect(TokenKind.KEYWORD, Keywords.FOR);
    expect(TokenKind.PAREN_LEFT);

    if (!match(TokenKind.SEMICOLON)) {
      initial = parseExpression();
    }

    expect(TokenKind.SEMICOLON);

    if (!match(TokenKind.SEMICOLON)) {
      condition = parseExpression();
    }

    expect(TokenKind.SEMICOLON);

    if (!match(TokenKind.PAREN_RIGHT)) {
      post = parseExpression();
    }

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final Block body = parseBlock();
    final ForLoopStatement node = new ForLoopStatement();

    node.setInitialExpression(initial);
    node.setConditionExpression(condition);
    node.setIteratorExpression(post);
    node.setBody(body);

    return node;

  }

  public LabelStatement parseLabelStatement() {

    final Identifier name = parseIdentifier();
    final LabelStatement node = new LabelStatement(name);

    expect(TokenKind.COLON);
    expectEndofStatement();

    return node;

  }

  public LoopStatement parseUntilStatement() {

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.UNTIL);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final LoopStatement node = new LoopStatement(LoopStatement.LoopKind.UNTIL, condition);
    final Block body = parseBlock();

    node.setBody(body);

    return node;

  }

  public LoopStatement parseWhileStatement() {

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.WHILE);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final LoopStatement node = new LoopStatement(LoopStatement.LoopKind.WHILE, condition);
    final Block body = parseBlock();

    node.setBody(body);

    return node;

  }

  public LoopStatement parseDoWhileStatement() {

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.DO);

    skip();

    final Block body = parseBlock();

    expect(TokenKind.KEYWORD, Keywords.WHILE);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    expectEndofStatement();

    final LoopStatement node = new LoopStatement(LoopStatement.LoopKind.DO, condition);

    node.setBody(body);

    return node;

  }

  public ConditionalStatement parseConditionalStatement() {

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.IF);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final Block body = parseBlock();
    final ConditionalStatement node = new ConditionalStatement(condition, body);

    skip();

    while (match(TokenKind.KEYWORD, Keywords.ELSEIF)) {
      accept();
      expect(TokenKind.PAREN_LEFT);
      parseExpression();
      expect(TokenKind.PAREN_RIGHT);
      skip();
      parseBlock();
      skip();
    }

    if (match(TokenKind.KEYWORD, Keywords.ELSE)) {
      accept();
      parseBlock(); // TODO: add else body to node
    }

    return node;

  }

  public ReturnStatement parseReturnStatement() {

    expect(TokenKind.KEYWORD, Keywords.RETURN);

    final ReturnStatement node = new ReturnStatement();

    if (!matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
      final Expression rhs = parseExpression();
      node.setExpression(rhs);
    }

    expectEndofStatement();

    return node;

  }

  public Expression parseExpression() {

    return parseAssignmentExpression();

  }

  public Expression parseNegationExpression() {

    return parsePrimaryExpression();

  }

  public Expression parseUnaryExpression() {

    if (!matchAny(TokenKind.PLUS, TokenKind.MINUS)) {
      return parseNegationExpression();
    }

    final Token token = accept();
    final UnaryExpression.Operator op;

    switch (token.kind) {
      case PLUS :
        op = UnaryExpression.Operator.POSITIVE;
        break;
      case MINUS:
        op = UnaryExpression.Operator.NEGATIVE;
        break;
      default:
        error("unrecognized unary operator");
        return null;
    }

    final Expression expr = parseUnaryExpression();

    return new UnaryExpression(op, expr);

  }

  public Expression parseMultiplicativeExpression() {

    final Expression lhs = parseUnaryExpression();

    if (!matchAny(TokenKind.MULTIPLY, TokenKind.DIVIDE, TokenKind.MODULUS)) {
      return lhs;
    }

    final Token token = accept();
    final MultiplicativeBinaryExpression.Operator op;

    switch (token.kind) {
      case MULTIPLY:
        op = MultiplicativeBinaryExpression.Operator.MULTIPLY;
        break;
      case DIVIDE:
        op = MultiplicativeBinaryExpression.Operator.DIVIDE;
        break;
      case MODULUS:
        op = MultiplicativeBinaryExpression.Operator.MODULUS;
        break;
      default:
        error("unrecognized multiplicative operator");
        return null;
    }

    final Expression rhs = parseMultiplicativeExpression();

    return new MultiplicativeBinaryExpression(op, lhs, rhs);

  }

  public Expression parseAdditiveExpression() {

    final Expression lhs = parseMultiplicativeExpression();

    if (!matchAny(TokenKind.PLUS, TokenKind.MINUS)) {
      return lhs;
    }

    final Token token = accept();
    final AdditiveBinaryExpression.Operator op;

    switch (token.kind) {
      case PLUS:
        op = AdditiveBinaryExpression.Operator.ADD;
        break;
      case MINUS:
        op = AdditiveBinaryExpression.Operator.SUBTRACT;
        break;
      default:
        error("unrecognized additive operator");
        return null;
    }

    final Expression rhs = parseAdditiveExpression();

    return new AdditiveBinaryExpression(op, lhs, rhs);

  }

  public Expression parseShiftExpression() {

    final Expression lhs = parseAdditiveExpression();

    if (!matchAny(TokenKind.SHIFT_LEFT, TokenKind.SHIFT_RIGHT)) {
      return lhs;
    }

    final Token token = accept();
    final ShiftBinaryExpression.Operator op;

    switch (token.kind) {
      case SHIFT_LEFT:
        op = ShiftBinaryExpression.Operator.SHL;
        break;
      case SHIFT_RIGHT:
        op = ShiftBinaryExpression.Operator.SHR;
        break;
      default:
        error("unrecognized binary shift operator");
        return null;
    }

    final Expression rhs = parseShiftExpression();

    return new ShiftBinaryExpression(op, lhs, rhs);

  }

  public Expression parseRelationalExpression() {

    final Expression lhs = parseShiftExpression();

    if (!matchAny(
            TokenKind.EQUALS, TokenKind.NOT_EQUALS,
            TokenKind.GREATER_THAN, TokenKind.GREATER_THAN_OR_EQUAL_TO,
            TokenKind.LESS_THAN, TokenKind.LESS_THAN_OR_EQUAL_TO)) {
      return lhs;
    }

    final Token token = accept();
    final RelationalBinaryExpression.Operator op;

    switch (token.kind) {
      case EQUALS:
        op = RelationalBinaryExpression.Operator.EQ;
        break;
      case NOT_EQUALS:
        op = RelationalBinaryExpression.Operator.NEQ;
        break;
      case GREATER_THAN:
        op = RelationalBinaryExpression.Operator.GT;
        break;
      case GREATER_THAN_OR_EQUAL_TO:
        op = RelationalBinaryExpression.Operator.GTE;
        break;
      case LESS_THAN:
        op = RelationalBinaryExpression.Operator.LT;
        break;
      case LESS_THAN_OR_EQUAL_TO:
        op = RelationalBinaryExpression.Operator.LTE;
        break;
      default:
        error("unrecognized relational operator");
        return null;
    }

    final Expression rhs = parseRelationalExpression();

    return new RelationalBinaryExpression(op, lhs, rhs);

  }

  public Expression parseLogicalExpression() {

    final Expression lhs = parseRelationalExpression();

    if (!matchAny(TokenKind.AND_LOGICAL, TokenKind.OR_LOGICAL)) {
      return lhs;
    }

    final Token token = accept();
    final LogicalBinaryExpression.Operator op;

    switch (token.kind) {
      case AND_LOGICAL:
        op = LogicalBinaryExpression.Operator.AND;
        break;
      case OR_LOGICAL:
        op = LogicalBinaryExpression.Operator.OR;
        break;
      default:
        error("unrecognized logical operator");
        return null;
    }

    final Expression rhs = parseLogicalExpression();

    return new LogicalBinaryExpression(op, lhs, rhs);

  }

  public Expression parseAssignmentExpression() {

    final Expression lhs = parseLogicalExpression();

    if (!matchAny(
            TokenKind.ASSIGN, TokenKind.ASSIGN_PLUS, TokenKind.ASSIGN_MINUS,
            TokenKind.ASSIGN_MULTIPLY, TokenKind.ASSIGN_DIVIDE, TokenKind.ASSIGN_MODULUS,
            TokenKind.ASSIGN_SHIFT_LEFT, TokenKind.ASSIGN_SHIFT_RIGHT,
            TokenKind.ASSIGN_POW, TokenKind.ASSIGN_AND, TokenKind.ASSIGN_OR, TokenKind.ASSIGN_XOR)) {
      return lhs;
    }

    final Token token = accept();
    final AssignmentExpression.Operator op;

    switch (token.kind) {
      case ASSIGN:
        op = AssignmentExpression.Operator.ASSIGN;
        break;
      case ASSIGN_PLUS:
        op = AssignmentExpression.Operator.ASSIGN_ADD;
        break;
      case ASSIGN_MINUS:
        op = AssignmentExpression.Operator.ASSIGN_SUB;
        break;
      case ASSIGN_MULTIPLY:
        op = AssignmentExpression.Operator.ASSIGN_MUL;
        break;
      case ASSIGN_DIVIDE:
        op = AssignmentExpression.Operator.ASSIGN_DIV;
        break;
      case ASSIGN_MODULUS:
        op = AssignmentExpression.Operator.ASSIGN_MOD;
        break;
      case ASSIGN_SHIFT_LEFT:
        op = AssignmentExpression.Operator.ASSIGN_SHL;
        break;
      case ASSIGN_SHIFT_RIGHT:
        op = AssignmentExpression.Operator.ASSIGN_SHR;
        break;
      case ASSIGN_AND:
        op = AssignmentExpression.Operator.ASSIGN_AND;
        break;
      case ASSIGN_OR:
        op = AssignmentExpression.Operator.ASSIGN_OR;
        break;
      case ASSIGN_XOR:
        op = AssignmentExpression.Operator.ASSIGN_XOR;
        break;
      default:
        error("unrecognized assignment operator");
        return null;
    }

    final Expression rhs = parseAssignmentExpression();

    return new AssignmentExpression(op, lhs, rhs);

  }

  public Expression parsePrimaryExpression() {

    if (match(TokenKind.PAREN_LEFT)) {
      expect(TokenKind.PAREN_LEFT);
      final Expression expr = parseExpression();
      expect(TokenKind.PAREN_RIGHT);
      return expr;
    }

    if (match(TokenKind.KEYWORD, Keywords.TRUE)) {
      accept();
      return new ConstantBooleanExpression(true);
    }

    if (match(TokenKind.KEYWORD, Keywords.FALSE)) {
      accept();
      return new ConstantBooleanExpression(false);
    }

    if (match(TokenKind.STRING)) {
      final Token token = accept();
      return new ConstantStringExpression(lexer.lexeme(token));
    }

    if (match(TokenKind.NUMBER)) {
      final Token token = accept();
      return new ConstantNumberExpression(
              Double.parseDouble(lexer.lexeme(token)));
    }

    if (match(TokenKind.IDENTIFIER)) {

      final Identifier ident = parseIdentifier();

      if (match(TokenKind.PAREN_LEFT)) {
        return parseCallExpression(ident);
      }

      return ident;

    }

    error("illegal expression");
    return null;

  }

  private CallExpression parseCallExpression(Identifier name) {

    expect(TokenKind.PAREN_LEFT);

    List<Expression> arguments = new ArrayList<>();

    while (!matchAny(TokenKind.PAREN_RIGHT, TokenKind.END)) {

      final Expression arg = parseExpression();
      if (arg != null)
        arguments.add(arg);

      if (match(TokenKind.COMMA))
        accept();

    }

    expect(TokenKind.PAREN_RIGHT);

    final CallExpression node = new CallExpression(name, arguments);

    return node;

  }

  private Parameter parseParameter() {

    final Identifier name = parseIdentifier();
    return new Parameter(name);

  }

  private Identifier parseIdentifier() {

    final Token token = expect(TokenKind.IDENTIFIER);
    final String lexeme = lexer.lexeme(token);

    return new Identifier(lexeme);

  }

  private Token accept() {

    return tokens.removeFirst();

  }

  private void expectEndofStatement() {

    // optional comment at end of statement

    if (match(TokenKind.COMMENT)) {
      accept();
    }

    // statements can end with the end of a block

    if (match(TokenKind.BRACE_RIGHT))
      return;

    // statements must end with line-break or semicolon

    if (!matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
      error("expected end of statement");
    }

    accept();

  }

  private Token expect(TokenKind kind) {

    return expect(kind, 0);

  }

  private Token expect(TokenKind kind, int tag) {

    if (match(kind, tag)) {
      final Token current = tokens.getFirst();
      accept();
      return current;
    }
    else {
      final Token current = tokens.getFirst();
      error(String.format("expected %s, found %s", kind, current.kind));
      return current;
    }

  }

  private boolean matchAll(TokenKind... kinds) {
    final int len = kinds.length;
    for (int i = 0; i < len; i++) {
      if (lookahead(i).kind != kinds[i])
        return false;
    }
    return true;
  }

  private boolean matchAny(TokenKind... kinds) {
    for (TokenKind kind : kinds) {
      if (match(kind)) {
        return true;
      }
    }
    return false;
  }

  private boolean match(TokenKind kind) {
    return match(kind, 0);
  }

  private boolean match(TokenKind kind, int tag) {
    final Token la = lookahead(0);
    if (kind == TokenKind.KEYWORD) {
      return (la.kind == kind && la.tag == tag);
    }
    return (la.kind == kind);
  }

  private Token lookahead(int k) {
    while (k >= tokens.size()) {
      tokens.addLast(lexer.scan());
    }
    return tokens.get(k);
  }

  private void skip() {
    while (matchAny(TokenKind.EOL, TokenKind.SEMICOLON, TokenKind.COMMENT))
      accept();
  }

  private void error(String message) {
    final Token token = tokens.get(0);
    final int line = token.line;
    final int column = token.column;
    final String msg = String.format("(%4d, %4d) %s", line, column, message);
    System.err.println(msg);
  }


}
