package net.rpgtoolkit.rpgcode.ir;

public class ClassFieldDeclaration implements Node {

    private Identifier name;
    private Visibility visibility;

    public ClassFieldDeclaration(Identifier name) {
        this.setName(name);
    }

    public Identifier getName() {
        return this.name;
    }

    public void setName(Identifier name) {
        if (name == null)
            throw new IllegalArgumentException();
        this.name = name;
    }

    public Visibility getVisibility() {
        return this.visibility;
    }

    public void setVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    @Override
    public void accept(NodeVisitor visitor) {
        visitor.visit(this);
    }

}
