package ast.variables;

public class AST_VAR_SIMPLE extends AST_VAR {
    public String name;

    public AST_VAR_SIMPLE(String name) {
        this.name = name;
    }

    @Override
    protected String name() {
        return "VAR(" + name + ")";
    }
}
