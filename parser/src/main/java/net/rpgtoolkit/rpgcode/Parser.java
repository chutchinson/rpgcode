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
  private final Stack<Token> tokens;
  private final Map<String, Integer> keywords;

  public Parser(Lexer lexer) {

    if (lexer == null) {
      throw new NullPointerException();
    }

    this.lexer = lexer;
    this.tokens = new Stack<>();
    this.keywords = new HashMap<>();
    this.keywords.put("function", Keywords.FUNCTION);
    this.keywords.put("method", Keywords.FUNCTION);
    this.keywords.put("return", Keywords.RETURN);
    this.keywords.put("public", Keywords.PUBLIC);
    this.keywords.put("private", Keywords.PRIVATE);
    this.keywords.put("protected", Keywords.PROTECTED);
    this.keywords.put("var", Keywords.VAR);
    this.keywords.put("virtual", Keywords.VIRTUAL);
    this.keywords.put("do", Keywords.DO);
    this.keywords.put("while", Keywords.WHILE);
    this.keywords.put("until", Keywords.UNTIL);
    this.keywords.put("loop", Keywords.LOOP);
    this.keywords.put("for", Keywords.FOR);
    this.keywords.put("break", Keywords.BREAK);
    this.keywords.put("continue", Keywords.CONTINUE);
    this.keywords.put("if", Keywords.IF);
    this.keywords.put("else", Keywords.ELSE);
    this.keywords.put("switch", Keywords.SWITCH);
    this.keywords.put("case", Keywords.CASE);
    this.keywords.put("default", Keywords.DEFAULT);
    this.keywords.put("null", Keywords.NULL);
    this.keywords.put("true", Keywords.TRUE);
    this.keywords.put("false", Keywords.FALSE);

  }

  public CompilationUnit parse() {

    final CompilationUnit unit = new CompilationUnit();

    while (!lookahead(TokenKind.END)) {

      if (lookaheadAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
        accept();
      }
      else if (matchKeyword(Keywords.FUNCTION)) {
        final FunctionDefinition fn = parseFunctionDefinition();
        if (fn != null) {
          unit.registerFunction(fn);
        }
      }
      else {
        final Token last = accept();
        final String message = String.format("unexpected token %s in program body", last);
        error(message);
        break;
      }

    }

    return unit;

  }

  public Statement parseStatement() {

    Statement stmt = null;

    while (lookaheadAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
      accept();
    }

    if (matchKeyword(Keywords.RETURN)) {
      stmt = parseReturnStatement();
    }
    else if (matchKeyword(Keywords.IF)) {
      stmt = parseConditionalStatement();
    }
    else if (matchKeyword(Keywords.DO)) {
      stmt = parseDoWhileStatement();
    }
    else if (matchKeyword(Keywords.WHILE)) {
      stmt = parseWhileStatement();
    }
    else if (matchKeyword(Keywords.UNTIL)) {
      stmt = parseUntilStatement();
    }
    else if (matchKeyword(Keywords.FOR)) {
      stmt = parseForLoopStatement();
    }
    else if (matchKeyword(Keywords.BREAK)) {
      stmt = new FlowControlStatement(FlowControlStatement.FlowControlKind.BREAK);
    }
    else if (matchKeyword(Keywords.CONTINUE)) {
      stmt = new FlowControlStatement(FlowControlStatement.FlowControlKind.CONTINUE);
    }
    else {
      final Expression expr = parseExpression();
      if (expr != null) {
        stmt = new ExpressionStatement(expr);
      }
      acceptLineTerminator();
    }

    return stmt;

  }

  public Expression parseAssignmentExpression() {

    final Expression symbol = parseLogicalExpression();

    if (!lookaheadAny(TokenKind.ASSIGN, TokenKind.ASSIGN_PLUS, TokenKind.ASSIGN_MINUS,
            TokenKind.ASSIGN_MULTIPLY, TokenKind.ASSIGN_DIVIDE, TokenKind.ASSIGN_MODULUS,
            TokenKind.ASSIGN_POW, TokenKind.ASSIGN_SHIFT_LEFT, TokenKind.ASSIGN_SHIFT_RIGHT,
            TokenKind.ASSIGN_AND, TokenKind.ASSIGN_OR, TokenKind.ASSIGN_XOR)) {
      return symbol;
    }

    Token token = accept();
    AssignmentExpression.Operator op;

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
      case ASSIGN_POW:
        op = AssignmentExpression.Operator.ASSIGN_POW;
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
        op = AssignmentExpression.Operator.ASSIGN;
        break;
    }

    final Expression rhs = parseAssignmentExpression();

    if (rhs == null) {
      error("expected expression for right-hand side of assignment");
      return null;
    }

    return new AssignmentExpression(op, symbol, rhs);

  }

  public ConditionalStatement parseConditionalStatement() {

    accept(TokenKind.PAREN_LEFT);

    final Expression condition = parseRelationalExpression();

    if (condition == null) {
      error("expected condition expression");
      return null;
    }

    accept(TokenKind.PAREN_RIGHT);

    final Block body = parseBlock();

    if (body == null) {
      error("conditional statement requires a body");
      return null;
    }

    return new ConditionalStatement(condition, body);

  }

  public LoopStatement parseDoWhileStatement() {

    final Block body = parseBlock();

    if (!matchKeyword(Keywords.WHILE)) {
      error("expected while keyword");
      return null;
    }

    accept(TokenKind.PAREN_LEFT);

    final Expression condition = parseRelationalExpression();

    if (condition == null) {
      error("expected loop condition expression");
      return null;
    }

    accept(TokenKind.PAREN_RIGHT);

    final LoopStatement node = new LoopStatement(LoopStatement.LoopKind.DO, condition);

    node.setBody(body);

    return node;

  }

  public LoopStatement parseUntilStatement() {

    accept(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    if (condition == null) {
      error("expected loop condition expression");
      return null;
    }

    accept(TokenKind.PAREN_RIGHT);

    final Block body = parseBlock();
    final LoopStatement node = new LoopStatement(LoopStatement.LoopKind.UNTIL, condition);

    node.setBody(body);

    return node;

  }

  public LoopStatement parseWhileStatement() {

    accept(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    if (condition == null) {
      error("expected loop condition expression");
      return null;
    }

    accept(TokenKind.PAREN_RIGHT);

    final Block body = parseBlock();
    final LoopStatement node = new LoopStatement(
            LoopStatement.LoopKind.WHILE, condition);

    node.setBody(body);

    return node;

  }

  public ForLoopStatement parseForLoopStatement() {

    accept(TokenKind.PAREN_LEFT);

    final Expression initial = parseExpression();

    accept(TokenKind.SEMICOLON);

    final Expression condition = parseRelationalExpression();

    accept(TokenKind.SEMICOLON);

    final Expression iterator = parseExpression();

    accept(TokenKind.PAREN_RIGHT);

    final Block body = parseBlock();
    final ForLoopStatement node = new ForLoopStatement();

    node.setInitialExpression(initial);
    node.setConditionExpression(condition);
    node.setIteratorExpression(iterator);
    node.setBody(body);

    return node;

  }

  public ReturnStatement parseReturnStatement() {

    ReturnStatement node = new ReturnStatement();

    if (!lookahead(TokenKind.EOL)) {
      final Expression rhs = parseExpression();
      node.setExpression(rhs);
    }

    acceptLineTerminator();

    return node;

  }

  public FunctionDefinition parseFunctionDefinition() {

    FunctionDefinition node;

    // parse function signature

    final Identifier name = parseIdentifier();

    if (name == null) {
      error("expected function name");
      return null;
    }

    node = new FunctionDefinition(name);

    // parse function parameters

    accept(TokenKind.PAREN_LEFT);

    while (!lookaheadAny(TokenKind.PAREN_RIGHT, TokenKind.END)) {

      final Parameter param = parseParameter();

      if (param != null)
        node.getParameters().add(param);

      if (lookahead(TokenKind.COMMA))
        accept();

    }

    accept(TokenKind.PAREN_RIGHT);

    // parse function body

    final Block block = parseBlock();

    if (block == null) {
      error("expected function body");
      return null;
    }

    node.setBody(block);

    return node;

  }

  public Parameter parseParameter() {

    Parameter node = null;

    final Identifier name = parseIdentifier();

    if (name != null) {
      node = new Parameter(name);
    }

    return node;

  }

  public Block parseBlock() {

    accept(TokenKind.BRACE_LEFT);

    Block block = new Block();

    while (!lookaheadAny(TokenKind.BRACE_RIGHT, TokenKind.END)) {
      final Statement statement = parseStatement();
      if (statement != null)
        block.getStatements().add(statement);
    }

    accept(TokenKind.BRACE_RIGHT);

    return block;

  }

  public Identifier parseIdentifier() {

    final Token ident = accept(TokenKind.IDENTIFIER);
    final String lexeme = lexer.lexeme(ident);

    return new Identifier(lexeme);

  }

  public Expression parseExpression() {

    final Expression lhs = parseAssignmentExpression();
    return lhs;

  }

  public Expression parseNegationExpression() {

    return parsePrimaryExpression();

  }

  public Expression parseUnaryExpression() {

    if (!lookaheadAny(TokenKind.PLUS, TokenKind.MINUS)) {
      return parseNegationExpression();
    }

    Token token = accept();
    UnaryExpression.Operator op;

    switch (token.kind) {
      case PLUS:
        op = UnaryExpression.Operator.POSITIVE;
        break;
      case MINUS:
        op = UnaryExpression.Operator.NEGATIVE;
        break;
      default:
        op = UnaryExpression.Operator.POSITIVE;
        break;
    }

    final Expression rhs = parseNegationExpression();

    return new UnaryExpression(op, rhs);

  }

  public Expression parseMultiplicativeExpression() {

    final Expression lhs = parseUnaryExpression();

    if (!lookaheadAny(TokenKind.MULTIPLY, TokenKind.DIVIDE, TokenKind.MODULUS)) {
      return lhs;
    }

    Token token = accept();
    MultiplicativeBinaryExpression.Operator op;

    switch (token.kind) {
      case DIVIDE:
        op = MultiplicativeBinaryExpression.Operator.DIVIDE;
        break;
      case MODULUS:
        op = MultiplicativeBinaryExpression.Operator.MODULUS;
        break;
      default:
        op = MultiplicativeBinaryExpression.Operator.MULTIPLY;
        break;
    }

    final Expression rhs = parseMultiplicativeExpression();

    return new MultiplicativeBinaryExpression(op, lhs, rhs);

  }

  public Expression parseShiftExpression() {

    final Expression lhs = parseMultiplicativeExpression();

    if (!lookaheadAny(TokenKind.SHIFT_LEFT, TokenKind.SHIFT_RIGHT)) {
      return lhs;
    }

    Token token = accept();
    ShiftBinaryExpression.Operator op;

    switch (token.kind) {
      case SHIFT_LEFT:
        op = ShiftBinaryExpression.Operator.SHL;
        break;
      case SHIFT_RIGHT:
        op = ShiftBinaryExpression.Operator.SHR;
        break;
      default:
        op = ShiftBinaryExpression.Operator.SHL;
        break;
    }

    final Expression rhs = parseShiftExpression();

    return new ShiftBinaryExpression(op, lhs, rhs);

  }

  public Expression parseAdditiveExpression() {

    final Expression lhs = parseShiftExpression();

    if (!lookaheadAny(TokenKind.PLUS, TokenKind.MINUS)) {
      return lhs;
    }

    Token token = accept();
    AdditiveBinaryExpression.Operator op;

    switch (token.kind) {
      case MINUS:
        op = AdditiveBinaryExpression.Operator.SUBTRACT;
        break;
      default:
        op = AdditiveBinaryExpression.Operator.ADD;
        break;
    }

    final Expression rhs = parseAdditiveExpression();

    return new AdditiveBinaryExpression(op, lhs, rhs);

  }

  public Expression parseRelationalExpression() {

    final Expression lhs = parseAdditiveExpression();

    if (!lookaheadAny(
            TokenKind.GREATER_THAN,
            TokenKind.GREATER_THAN_OR_EQUAL_TO,
            TokenKind.LESS_THAN,
            TokenKind.LESS_THAN_OR_EQUAL_TO,
            TokenKind.EQUALS, TokenKind.NOT_EQUALS)) {
      return lhs;
    }

    Token token = accept();
    RelationalBinaryExpression.Operator op;

    switch (token.kind) {
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
      case NOT_EQUALS:
        op = RelationalBinaryExpression.Operator.NEQ;
        break;
      default:
        op = RelationalBinaryExpression.Operator.EQ;
        break;
    }

    final Expression rhs = parseRelationalExpression();

    return new RelationalBinaryExpression(op, lhs, rhs);

  }

  public Expression parseLogicalExpression() {

    final Expression lhs = parseRelationalExpression();

    if (!lookaheadAny(TokenKind.AND_LOGICAL, TokenKind.OR_LOGICAL)) {
      return lhs;
    }

    Token token = accept();
    LogicalBinaryExpression.Operator op;

    switch (token.kind) {
      case AND_LOGICAL:
        op = LogicalBinaryExpression.Operator.AND;
        break;
      case OR_LOGICAL:
        op = LogicalBinaryExpression.Operator.OR;
        break;
      default:
        op = LogicalBinaryExpression.Operator.AND;
        break;
    }

    final Expression rhs = parseLogicalExpression();

    return new LogicalBinaryExpression(op, lhs, rhs);

  }

  public Expression parsePrimaryExpression() {

    if (match(TokenKind.PAREN_LEFT)) {
      final Expression expr = parseExpression();
      accept(TokenKind.PAREN_RIGHT);
      return expr;
    }

    if (matchKeyword(Keywords.TRUE)) return new ConstantBooleanExpression(true);
    if (matchKeyword(Keywords.FALSE)) return new ConstantBooleanExpression(false);

    if (lookahead(TokenKind.NUMBER)) return parseConstantNumberExpression();
    if (lookahead(TokenKind.STRING)) return parseConstantStringExpression();

    if (lookahead(TokenKind.IDENTIFIER)) {
      final Identifier ident = parseIdentifier();
      if (lookahead(TokenKind.PAREN_LEFT)) {
        return parseCallExpression(ident);
      }
      return ident;
    }

    return null;

  }

  public CallExpression parseCallExpression(Expression lhs) {

    if (lhs == null)
      throw new IllegalArgumentException();

    accept(TokenKind.PAREN_LEFT);

    List<Expression> arguments = new ArrayList<>();

    while (!lookaheadAny(TokenKind.PAREN_RIGHT, TokenKind.END)) {
      final Expression arg = parseExpression();
      arguments.add(arg);
      if (lookahead(TokenKind.COMMA)) {
        accept(TokenKind.COMMA);
      }
    }

    accept(TokenKind.PAREN_RIGHT);

    final CallExpression node = new CallExpression(lhs, arguments);

    return node;

  }

  public ConstantNumberExpression parseConstantNumberExpression() {

    final String lexeme = lexer.lexeme(accept());
    final double value = Double.parseDouble(lexeme);

    return new ConstantNumberExpression(value);

  }

  public ConstantStringExpression parseConstantStringExpression() {

    final String lexeme = lexer.lexeme(accept());

    return new ConstantStringExpression(lexeme);

  }

  public Token current() {
    return tokens.firstElement();
  }

  public Token accept() {
    return tokens.pop();
  }

  public void acceptLineTerminator() {
    if (lookaheadAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
      accept();
    }
  }

  public Token accept(TokenKind kind) {

    if (lookahead(kind)) {
      return accept();
    }
    else {
      final Token token = current();
      final String message = String.format("expected token %s, found %s", kind, token.kind);
      error(message);
      return token;
    }

  }

  public boolean matchKeyword(int id) {

    if (!lookahead(TokenKind.IDENTIFIER))
      return false;

    final String lexeme = lexer.lexeme(current());

    if (lexeme == null || !keywords.containsKey(lexeme))
      return false;

    final int keyword = keywords.get(lexeme);

    if (keyword != id)
      return false;

    accept();
    return true;

  }

  public boolean lookaheadAny(TokenKind... kinds) {

    for (TokenKind kind : kinds) {
      if (lookahead(kind)) {
        return true;
      }
    }

    return false;

  }

  public boolean match(TokenKind... kinds) {

    if (!lookahead(kinds)) return false;

    for (int i = 0; i < kinds.length; i++) {
      accept();
    }

    return true;

  }

  public boolean lookahead(TokenKind... kinds) {

    final int count = kinds.length;
    for (int i = 0; i < count; i++) {
      if (lookahead(i).kind != kinds[i]) return false;
    }

    return true;

  }

  public void error(String message) {

    System.err.println("error: " + message);

  }

  private Token lookahead(int k) {

    while (k >= tokens.size()) {
      tokens.push(lexer.scan());
    }

    return tokens.get(k);

  }



}
