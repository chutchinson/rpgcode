package net.rpgtoolkit.rpgcode.ir;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit implements Node {

    private final List<ClassDeclaration> classes;
    private final List<FunctionDeclaration> functions;

    public CompilationUnit() {
        this.classes = new ArrayList<>();
        this.functions = new ArrayList<>();
    }

    public List<ClassDeclaration> getClassDeclarations() {
        return this.classes;
    }

    public List<FunctionDeclaration> getFunctionDeclarations() {
        return this.functions;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
