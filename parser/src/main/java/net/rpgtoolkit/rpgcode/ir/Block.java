package net.rpgtoolkit.rpgcode.ir;

import java.util.LinkedList;
import java.util.List;

public class Block implements Node {

    private final List<Statement> statements;

    public Block() {
        this.statements = new LinkedList<>();
    }

    public List<Statement> getStatements() {
        return this.statements;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
