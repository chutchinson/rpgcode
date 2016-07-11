package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class ConstantBooleanExpression implements ConstantExpression {

    private boolean value;

    public ConstantBooleanExpression(boolean value) {
        this.value = value;
    }

    public boolean getValue() {
        return this.value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
