package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class AdditiveBinaryExpression extends BinaryExpression {

    public enum Operator {
        ADD,
        SUBTRACT
    }

    private Operator op;

    public AdditiveBinaryExpression(Operator op, Expression left, Expression right) {
        super(left, right);
        this.op = op;
    }

    public Operator getOperator() {
        return this.op;
    }

    public void setOperator(Operator op) {
        this.op = op;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
