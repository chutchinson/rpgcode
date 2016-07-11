package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;

public class FlowControlStatement implements Statement {

    public enum FlowControlKind {
        BREAK,
        CONTINUE,
        GOTO
    }

    private FlowControlKind kind;

    public FlowControlStatement(FlowControlKind kind) {
        setKind(kind);
    }

    public FlowControlKind getKind() {
        return this.kind;
    }

    public void setKind(FlowControlKind kind) {
        this.kind = kind;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
