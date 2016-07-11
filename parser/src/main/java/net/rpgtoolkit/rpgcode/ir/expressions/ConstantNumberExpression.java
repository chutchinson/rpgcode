package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class ConstantNumberExpression implements ConstantExpression {

    private double value;

    public ConstantNumberExpression(double value) {
        this.value = value;
    }

    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
