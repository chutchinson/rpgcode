package net.rpgtoolkit.rpgcode.ir.statements;

import net.rpgtoolkit.rpgcode.ir.Identifier;
import net.rpgtoolkit.rpgcode.ir.NodeVisitor;
import net.rpgtoolkit.rpgcode.ir.Statement;

public class LabelStatement implements Statement {

    private Identifier name;

    public LabelStatement(Identifier name) {
        setName(name);
    }

    public Identifier getName() {
        return this.name;
    }

    public void setName(Identifier name) {
        if (name == null)
            throw new IllegalArgumentException();
        this.name = name;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
