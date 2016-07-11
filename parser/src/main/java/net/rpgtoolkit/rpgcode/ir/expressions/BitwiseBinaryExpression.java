package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class BitwiseBinaryExpression extends BinaryExpression {

    public enum Operator {
        AND,
        OR,
        XOR
    }

    private Operator op;

    public BitwiseBinaryExpression(Operator op, Expression lhs, Expression rhs) {
        super(lhs, rhs);
        setOperator(op);
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
