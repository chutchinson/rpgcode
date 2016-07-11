package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;

public class ReturnStatement implements Statement {

    private Expression expr;

    public Expression getExpression() {
        return this.expr;
    }

    public void setExpression(Expression expr) {
        this.expr = expr;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
