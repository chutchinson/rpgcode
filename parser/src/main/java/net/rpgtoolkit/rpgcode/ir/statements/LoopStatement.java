package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.Block;
import net.rpgtoolkit.rpgcode.ir.Expression;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;

public class LoopStatement implements Statement {

    public enum LoopKind {
        DO,
        WHILE,
        UNTIL
    }

    private LoopKind kind;
    private Expression condition;
    private Block body;

    public LoopStatement(LoopKind kind, Expression condition) {
        setKind(kind);
        setCondition(condition);
    }

    public LoopKind getKind() {
        return this.kind;
    }

    public void setKind(LoopKind kind) {
        this.kind = kind;
    }

    public Expression getCondition() {
        return this.condition;
    }

    public void setCondition(Expression condition) {
        if (condition == null)
            throw new IllegalArgumentException();
        this.condition = condition;
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
