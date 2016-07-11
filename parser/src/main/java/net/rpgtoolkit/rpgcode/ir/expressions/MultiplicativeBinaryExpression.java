package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class MultiplicativeBinaryExpression extends BinaryExpression {

    public enum Operator {
        MULTIPLY,
        DIVIDE,
        MODULUS,
        POWER
    }

    private Operator op;

    public MultiplicativeBinaryExpression(Operator op, Expression left, Expression right) {
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
