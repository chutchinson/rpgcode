package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class PostfixExpression implements Expression {

    public enum Operator {
        INCREMENT,
        DECREMENT
    }

    private Operator op;
    private Expression lhs;

    public PostfixExpression(Operator op, Expression lhs) {
        setOperator(op);
        setExpression(lhs);
    }

    public Operator getOperator() {
        return this.op;
    }

    public void setOperator(Operator op) {
        this.op = op;
    }

    public Expression getExpression() {
        return this.lhs;
    }

    public void setExpression(Expression expr) {
        this.lhs = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
