package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class AssignmentExpression implements Expression {

    public enum Operator {
        ASSIGN,
        ASSIGN_ADD,
        ASSIGN_SUB,
        ASSIGN_MUL,
        ASSIGN_DIV,
        ASSIGN_MOD,
        ASSIGN_POW,
        ASSIGN_SHL,
        ASSIGN_SHR,
        ASSIGN_AND,
        ASSIGN_OR,
        ASSIGN_XOR
    }

    private Operator op;
    private Expression symbol;
    private Expression value;

    public AssignmentExpression(Operator op, Expression sym, Expression value) {
        setOperator(op);
        setSymbolExpression(sym);
        setValueExpression(value);
    }

    public Operator getOperator() {
        return this.op;
    }

    public void setOperator(Operator op) {
        this.op = op;
    }

    public Expression getSymbolExpression() {
        return this.symbol;
    }

    public void setSymbolExpression(Expression sym) {
        if (sym == null)
            throw new IllegalArgumentException();
        this.symbol = sym;
    }

    public Expression getValueExpression() {
        return this.value;
    }

    public void setValueExpression(Expression value) {
        if (value == null)
            throw new IllegalArgumentException();
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
