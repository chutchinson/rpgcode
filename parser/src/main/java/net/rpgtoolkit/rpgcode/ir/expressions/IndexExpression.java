package net.rpgtoolkit.rpgcode.ir.expressions;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.Identifier;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;

public class IndexExpression implements Expression {

    private Identifier symbol;
    private Expression index;

    public IndexExpression(Identifier sym) {
        setSymbol(sym);
    }

    public Identifier getSymbol() {
        return this.symbol;
    }

    public void setSymbol(Identifier sym) {
        if (sym == null)
            throw new IllegalArgumentException();
        this.symbol = sym;
    }

    public Expression getIndexExpression() {
        return this.index;
    }

    public void setIndexExpression(Expression expr) {
        this.index = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }
}
