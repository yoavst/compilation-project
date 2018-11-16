package ast.declarations;

public abstract class AST_DEC_VAR extends AST_DEC {
    public AST_DEC_VAR(String type, String name) {
        super(type, name);
    }

    @Override
    protected String name() {
        return "Unknown var Dec";
    }
}