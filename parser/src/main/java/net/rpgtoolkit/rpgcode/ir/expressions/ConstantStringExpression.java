package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class ConstantStringExpression implements ConstantExpression {

    private String value;

    public ConstantStringExpression(String value) {
        setValue(value);
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
