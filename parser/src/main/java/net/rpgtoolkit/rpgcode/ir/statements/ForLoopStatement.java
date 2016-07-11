package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.Block;
import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;
import net.rpgtoolkit.rpgcode.ir.expressions.RelationalBinaryExpression;

public class ForLoopStatement implements Statement {

    private Expression initial;
    private Expression condition;
    private Expression iterator;
    private Block body;

    public Expression getInitialExpression() {
        return this.initial;
    }

    public void setInitialExpression(Expression expr) {
        this.initial = expr;
    }

    public Expression getConditionExpression() {
        return this.condition;
    }

    public void setConditionExpression(Expression condition) {
        this.condition = condition;
    }

    public Expression getIteratorExpression() {
        return this.iterator;
    }

    public void setIteratorExpression(Expression expr) {
        this.iterator = expr;
    }

    public Block getBody() {
        return this.body;
    }

    public void setBody(Block body) {
        this.body = body;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
