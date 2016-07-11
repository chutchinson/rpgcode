package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.Block;
import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;
import net.rpgtoolkit.rpgcode.ir.expressions.RelationalBinaryExpression;

public class ConditionalStatement implements Statement {

    private Expression condition;
    private Block body;

    public ConditionalStatement(Expression expr, Block body) {
        setConditionExpression(expr);
        setBody(body);
    }

    public Expression getConditionExpression() {
        return this.condition;
    }

    public void setConditionExpression(Expression expr) {
        if (expr == null)
            throw new IllegalArgumentException();
        this.condition = expr;
    }

    public Block getBody() {
        return this.body;
    }

    public void setBody(Block body) {
        if (body == null)
            throw new IllegalArgumentException();
        this.body = body;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
