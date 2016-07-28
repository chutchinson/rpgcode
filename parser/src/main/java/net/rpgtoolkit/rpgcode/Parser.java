/**
 * Copyright (c) 2015, rpgtoolkit.net <help@rpgtoolkit.net>
 * <p>
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package net.rpgtoolkit.rpgcode;

import net.rpgtoolkit.blade.ir.*;
import net.rpgtoolkit.blade.ir.expressions.*;
import net.rpgtoolkit.blade.ir.statements.*;

import java.util.*;

/**
 * RPGCode reference parser
 */
public class Parser {

  private final Lexer lexer;
  private final LinkedList<Token> tokens;
  private final Set<ParserListener> listeners;
  private final SourceLocationBuilder loc;

  public Parser(Lexer lexer) {

    if (lexer == null) {
      throw new NullPointerException();
    }

    this.lexer = lexer;
    this.tokens = new LinkedList<>();
    this.listeners = new HashSet<>();
    this.loc = new SourceLocationBuilder(this.lexer);

  }

  public void addListener(ParserListener listener) {
    if (!listeners.contains(listener))
      listeners.add(listener);
  }

  public void removeListener(ParserListener listener) {
    listeners.remove(listener);
  }

  public CompilationUnit parse() {

    final CompilationUnit node = new CompilationUnit(loc.newRange(), "???");

    while (!match(TokenKind.END)) {

      if (match(TokenKind.COMMENT)) {
        accept();
        continue;
      }

      if (match(TokenKind.KEYWORD, Keywords.CLASS)) {
        final ClassDeclaration decl = parseClassDeclaration();
        if (decl != null)
          node.getClassDeclarations().add(decl);
      } else if (match(TokenKind.KEYWORD, Keywords.FUNCTION) || match(TokenKind.KEYWORD, Keywords.INLINE)) {
        final FunctionDeclaration decl = parseFunctionDeclaration();
        if (decl != null)
          node.getFunctionDeclarations().add(decl);
      } else if (matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
        accept();
      } else {
        final Statement stmt = parseStatement();
        if (stmt == null) {
          error("unexpected token in program body");
          accept();
        }
      }

    }

    loc.end(node.getSourceRange());

    return node;

  }

  public ClassFieldDeclaration parseClassFieldDeclaration() {

    final SourceRange range = loc.newRange();

    if (match(TokenKind.KEYWORD, Keywords.VAR)) {
      accept();
    }

    final Identifier name = parseIdentifier();
    final ClassFieldDeclaration node = new ClassFieldDeclaration(range, name);

    expectEndofStatement();

    loc.end(range);

    return node;

  }

  public ClassDeclaration parseClassDeclaration() {

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.CLASS);

    final Identifier name = parseIdentifier();
    final ClassDeclaration node = new ClassDeclaration(range, name);

    // TODO: parse inheritance list

    skip();
    expect(TokenKind.BRACE_LEFT);

    Visibility currentVisibility = Visibility.PUBLIC;

    while (!matchAny(TokenKind.BRACE_RIGHT, TokenKind.END)) {

      skip();

      if (match(TokenKind.KEYWORD, Keywords.PUBLIC)) {
        currentVisibility = parseVisibilityLabel();
      } else if (match(TokenKind.KEYWORD, Keywords.PRIVATE)) {
        currentVisibility = parseVisibilityLabel();
      } else if (match(TokenKind.KEYWORD, Keywords.PROTECTED)) {
        currentVisibility = parseVisibilityLabel();
      } else if (match(TokenKind.KEYWORD, Keywords.FUNCTION) || match(TokenKind.KEYWORD, Keywords.INLINE)) {
        final FunctionDeclaration fn = parseFunctionDeclaration();
        if (fn != null) {
          fn.setVisibility(currentVisibility);
          node.getFunctionDeclarations().add(fn);
        }
      } else if (match(TokenKind.KEYWORD, Keywords.VAR) || match(TokenKind.IDENTIFIER)) {
        final ClassFieldDeclaration field = parseClassFieldDeclaration();
        if (field != null) {
          field.setVisibility(currentVisibility);
          node.getFieldDeclarations().add(field);
        }
      } else {
        error("unexpected token in class declaration");
        accept();
      }

    }

    expect(TokenKind.BRACE_RIGHT);

    loc.end(range);

    return node;

  }

  private Visibility parseVisibilityLabel() {

    Visibility result;

    if (match(TokenKind.KEYWORD, Keywords.PUBLIC)) {
      accept();
      result = Visibility.PUBLIC;
    }
    else if (match(TokenKind.KEYWORD, Keywords.PRIVATE)) {
      accept();
      result = Visibility.PRIVATE;
    }
    else if (match(TokenKind.KEYWORD, Keywords.PROTECTED)) {
      accept();
      result = Visibility.PROTECTED;
    }
    else {
      error("unrecognized visibility");
      result = Visibility.PRIVATE;
    }

    expect(TokenKind.COLON);
    expectEndofStatement();

    return result;

  }

  public FunctionDeclaration parseFunctionDeclaration() {

    boolean inline = false;

    final SourceRange range = loc.newRange();

    if (match(TokenKind.KEYWORD, Keywords.INLINE)) {
      accept();
      inline = true;
    }

    expect(TokenKind.KEYWORD, Keywords.FUNCTION);

    final Identifier name = parseIdentifier();
    final FunctionDeclaration node = new FunctionDeclaration(range, name);

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
    } else {
      skip();
      final Block block = parseBlock();
      node.setBody(block);
    }

    loc.end(range);

    return node;

  }

  private Block parseBlock() {

    final SourceRange range = loc.newRange();
    final Block node = new Block(SourceRange.empty());
    final NodeCollection<Statement> statements = node.getStatements();

    expect(TokenKind.BRACE_LEFT);

    while (true) {

      skip();

      if (!matchAny(TokenKind.BRACE_RIGHT, TokenKind.END)) {
        final Statement stmt = parseStatement();
        statements.add(stmt);
      } else {
        break;
      }

    }

    expect(TokenKind.BRACE_RIGHT);

    loc.end(range);

    return node;

  }

  public Statement parseStatement() {

    // parse comments from the ancient 2.x version of the grammar
    // where asterisk starts a comment

    if (match(TokenKind.MULTIPLY)) {
      while (!matchAny(TokenKind.EOL, TokenKind.END)) {
        accept();
      }
    }

    // skip blank lines

    skip();

    if (match(TokenKind.KEYWORD, Keywords.RETURN)) {
      return parseReturnStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.IF)) {
      return parseConditionalStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.DO)) {
      return parseDoWhileStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.WHILE)) {
      return parseWhileStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.UNTIL)) {
      return parseUntilStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.FOR)) {
      return parseForStatement();
    } else if (matchAll(TokenKind.IDENTIFIER, TokenKind.COLON)) {
      return parseLabelStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.BREAK)) {
      return parseBreakStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.CONTINUE)) {
      return parseContinueStatement();
    } else if (match(TokenKind.KEYWORD, Keywords.ON)) {
      return parseErrorHandlerStatement();
    } else {
      return parseExpressionStatement();
    }

  }

  public FlowControlStatement parseContinueStatement() {

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.CONTINUE);
    expectEndofStatement();

    loc.end(range);

    return new FlowControlStatement(
        range, FlowControlStatement.FlowControlKind.CONTINUE);

  }

  public FlowControlStatement parseBreakStatement() {

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.BREAK);
    expectEndofStatement();

    loc.end(range);

    return new FlowControlStatement(
        range, FlowControlStatement.FlowControlKind.BREAK);

  }

  public ErrorHandlerStatement parseErrorHandlerStatement() {

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.ON);
    expect(TokenKind.KEYWORD, Keywords.ERROR);
    expect(TokenKind.KEYWORD, Keywords.RESUME);
    expect(TokenKind.KEYWORD, Keywords.NEXT);

    loc.end(range);

    return new ErrorHandlerStatement(
        range, ErrorHandlerStatement.ErrorHandlerKind.RESUME);

  }

  public ExpressionStatement parseExpressionStatement() {

    final SourceRange range = loc.newRange();
    final Expression expr = parseExpression();

    if (expr == null) {
      error("expected expression");
      return null;
    }

    expectEndofStatement();

    loc.end(range);

    return new ExpressionStatement(range, expr);

  }

  public ForLoopStatement parseForStatement() {

    Expression initial = null;
    Expression condition = null;
    Expression post = null;

    final SourceRange range = loc.newRange();

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
    final ForLoopStatement node = new ForLoopStatement(range);

    node.setInitialExpression(initial);
    node.setConditionExpression(condition);
    node.setIteratorExpression(post);
    node.setBody(body);

    loc.end(range);

    return node;

  }

  public LabelStatement parseLabelStatement() {

    final SourceRange range = loc.newRange();

    final Identifier name = parseIdentifier();
    final LabelStatement node = new LabelStatement(range, name);

    expect(TokenKind.COLON);
    expectEndofStatement();

    loc.end(range);

    return node;

  }

  public LoopStatement parseUntilStatement() {

    // TODO: for statement body parse block or single statement

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.UNTIL);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final LoopStatement node = new LoopStatement(range, LoopStatement.LoopKind.UNTIL, condition);
    final Block body = parseBlock();

    node.setBody(body);

    loc.end(range);

    return node;

  }

  public LoopStatement parseWhileStatement() {

    final SourceRange range = loc.newRange();

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.WHILE);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final LoopStatement node = new LoopStatement(range, LoopStatement.LoopKind.WHILE, condition);
    final Block body = parseBlock();

    node.setBody(body);

    loc.end(range);

    return node;

  }

  public LoopStatement parseDoWhileStatement() {

    final SourceRange range = loc.newRange();

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.DO);

    skip();

    final Block body = parseBlock();

    expect(TokenKind.KEYWORD, Keywords.WHILE);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    expectEndofStatement();

    final LoopStatement node = new LoopStatement(range, LoopStatement.LoopKind.DO, condition);

    node.setBody(body);

    loc.end(range);

    return node;

  }

  public ConditionalStatement parseConditionalStatement() {

    final SourceRange range = loc.newRange();

    // TODO: for statement body parse block or single statement

    expect(TokenKind.KEYWORD, Keywords.IF);
    expect(TokenKind.PAREN_LEFT);

    final Expression condition = parseExpression();

    expect(TokenKind.PAREN_RIGHT);
    skip();

    final Block body = parseBlock();
    final ConditionalStatement node = new ConditionalStatement(range, condition, body);

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

    loc.end(range);

    return node;

  }

  public ReturnStatement parseReturnStatement() {

    final SourceRange range = loc.newRange();

    expect(TokenKind.KEYWORD, Keywords.RETURN);

    final ReturnStatement node = new ReturnStatement(range);

    if (!matchAny(TokenKind.EOL, TokenKind.SEMICOLON)) {
      final Expression rhs = parseExpression();
      node.setExpression(rhs);
    }

    expectEndofStatement();

    loc.end(range);

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

    final SourceRange range = loc.newRange();
    final Token token = accept();
    final UnaryExpression.Operator op;

    switch (token.kind) {
      case PLUS:
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

    loc.end(range);

    return new UnaryExpression(range, op, expr);

  }

  public Expression parseMultiplicativeExpression() {

    final SourceRange range = loc.newRange();
    final Expression lhs = parseUnaryExpression();

    if (!matchAny(TokenKind.MULTIPLY, TokenKind.DIVIDE, TokenKind.DIVIDE_INT, TokenKind.MODULUS)) {
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
      case DIVIDE_INT:
        op = MultiplicativeBinaryExpression.Operator.DIVIDE_INT;
        break;
      case MODULUS:
        op = MultiplicativeBinaryExpression.Operator.MODULUS;
        break;
      default:
        error("unrecognized multiplicative operator");
        return null;
    }

    final Expression rhs = parseMultiplicativeExpression();

    loc.end(range);

    return new MultiplicativeBinaryExpression(range, op, lhs, rhs);

  }

  public Expression parseAdditiveExpression() {

    final SourceRange range = loc.newRange();
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

    loc.end(range);

    return new AdditiveBinaryExpression(range, op, lhs, rhs);

  }

  public Expression parseShiftExpression() {

    final SourceRange range = loc.newRange();
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

    loc.end(range);

    return new ShiftBinaryExpression(range, op, lhs, rhs);

  }

  public Expression parseRelationalExpression() {

    final SourceRange range = loc.newRange();
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

    loc.end(range);

    return new RelationalBinaryExpression(range, op, lhs, rhs);

  }

  public Expression parseLogicalExpression() {

    final SourceRange range = loc.newRange();
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

    loc.end(range);

    return new LogicalBinaryExpression(range, op, lhs, rhs);

  }

  public Expression parseAssignmentExpression() {

    final SourceRange range = loc.newRange();
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

    loc.end(range);

    return new AssignmentExpression(range, op, lhs, rhs);

  }

  public Expression parsePrimaryExpression() {

    if (match(TokenKind.PAREN_LEFT)) {
      expect(TokenKind.PAREN_LEFT);
      final Expression expr = parseExpression();
      expect(TokenKind.PAREN_RIGHT);
      return expr;
    }

    final SourceRange range = loc.newRange();

    if (match(TokenKind.KEYWORD, Keywords.TRUE)) {
      accept();
      loc.end(range);
      return new ConstantBooleanExpression(range, true);
    }

    if (match(TokenKind.KEYWORD, Keywords.FALSE)) {
      accept();
      loc.end(range);
      return new ConstantBooleanExpression(range, false);
    }

    if (match(TokenKind.STRING)) {
      final Token token = accept();
      loc.end(range);
      return new ConstantStringExpression(range, lexer.lexeme(token));
    }

    if (match(TokenKind.NUMBER)) {
      final Token token = accept();
      loc.end(range);
      return new ConstantNumberExpression(range, Double.parseDouble(lexer.lexeme(token)));
    }

    if (matchAll(TokenKind.IDENTIFIER, TokenKind.PAREN_LEFT)) {
      return parseCallExpression();
    } else if (matchAll(TokenKind.IDENTIFIER, TokenKind.BRACKET_LEFT)) {
      return parseIndexExpression();
    } else if (matchAll(TokenKind.IDENTIFIER, TokenKind.INCREMENT)) {
      return parsePostfixExpression();
    } else if (matchAll(TokenKind.IDENTIFIER, TokenKind.DECREMENT)) {
      return parsePostfixExpression();
    } else if (match(TokenKind.IDENTIFIER)) {
      return parseIdentifier();
    }

    error("unrecognized expression");
    return null;

  }

  private Expression parsePostfixExpression() {

    final SourceRange range = loc.newRange();
    final Identifier lhs = parseIdentifier();

    if (!matchAny(TokenKind.INCREMENT, TokenKind.DECREMENT)) {
      return lhs;
    }

    final Token token = accept();
    final PostfixExpression.Operator op;

    switch (token.kind) {
      case INCREMENT:
        op = PostfixExpression.Operator.INCREMENT;
        break;
      case DECREMENT:
        op = PostfixExpression.Operator.DECREMENT;
        break;
      default:
        error("unrecognized postfix operator");
        return null;
    }

    loc.end(range);

    return new PostfixExpression(range, op, lhs);

  }

  private CallExpression parseCallExpression() {

    final SourceRange range = loc.newRange();
    final Identifier symbol = parseIdentifier();

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

    loc.end(range);

    final CallExpression node = new CallExpression(range, symbol, arguments);

    return node;

  }

  private IndexExpression parseIndexExpression() {

    final SourceRange range = loc.newRange();
    final Identifier symbol = parseIdentifier();
    final IndexExpression node = new IndexExpression(range, symbol);

    expect(TokenKind.BRACKET_LEFT);

    if (!match(TokenKind.BRACKET_RIGHT)) {
      final Expression index = parsePrimaryExpression();
      node.setIndexExpression(index);
    }

    expect(TokenKind.BRACKET_RIGHT);

    // skip type hint

    if (matchAny(TokenKind.DOLLAR, TokenKind.NOT)) {
      accept();
    }

    loc.end(range);

    return node;

  }

  private Parameter parseParameter() {

    final SourceRange range = loc.newRange();
    final Identifier name = parseIdentifier();

    loc.end(range);

    return new Parameter(range, name);

  }

  private Identifier parseIdentifier() {

    final SourceRange range = loc.newRange();

    if (match(TokenKind.HASH)) {
      accept();
    }

    final Token token = expect(TokenKind.IDENTIFIER);
    final String lexeme = lexer.lexeme(token);

    if (matchAny(TokenKind.DOLLAR, TokenKind.NOT))
      accept();

    loc.end(range);

    return new Identifier(range, lexeme);

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
      return accept();
    } else {
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
    for (final ParserListener listener : listeners) {
      listener.error(new ParseError(line, column, msg));
    }
    accept();
  }


}
