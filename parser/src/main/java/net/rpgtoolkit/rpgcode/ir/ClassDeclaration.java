package net.rpgtoolkit.rpgcode.ir;

import java.util.ArrayList;
import java.util.List;

public class ClassDeclaration implements Node {

    private Identifier name;
    private final List<Identifier> inheritanceList;
    private final List<FunctionDeclaration> functions;
    private final List<ClassFieldDeclaration> fields;

    public ClassDeclaration(Identifier name) {
        this.functions = new ArrayList<>();
        this.fields = new ArrayList<>();
        this.inheritanceList = new ArrayList<>();
        setName(name);
    }

    public List<Identifier> getInheritanceList() {
        return this.inheritanceList;
    }

    public List<FunctionDeclaration> getFunctionDeclarations() {
        return this.functions;
    }

    public List<ClassFieldDeclaration> getFieldDeclarations() {
        return this.fields;
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
