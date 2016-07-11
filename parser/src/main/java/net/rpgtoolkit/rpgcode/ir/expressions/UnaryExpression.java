package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class UnaryExpression implements Expression {

    public enum Operator {
        POSITIVE,
        NEGATIVE
    }

    private Operator op;
    private Expression expr;

    public UnaryExpression(Operator op, Expression expr) {
        setOperator(op);
        setExpression(expr);
    }

    public Operator getOperator() {
        return this.op;
    }

    public void setOperator(Operator op) {
        this.op = op;
    }

    public Expression getExpression() {
        return this.expr;
    }

    public void setExpression(Expression expr) {
        if (expr == null)
            throw new IllegalArgumentException();
        this.expr = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
