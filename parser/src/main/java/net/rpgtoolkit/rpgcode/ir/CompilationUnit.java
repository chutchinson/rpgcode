package net.rpgtoolkit.rpgcode.ir;

import java.util.ArrayList;
import java.util.List;

public class CompilationUnit implements Node {

    private final List<FunctionDefinition> functions;

    public CompilationUnit() {
        this.functions = new ArrayList<>();
    }

    public List<FunctionDefinition> getFunctionDefinitions() {
        return this.functions;
    }

    public void registerFunction(FunctionDefinition fn) {
        this.functions.add(fn);
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
