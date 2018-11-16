package ast.declarations;

public class AST_DEC_ARRAY extends AST_DEC {
    public AST_DEC_ARRAY(String type, String name) {
        super(type, name);
    }

    @Override
    protected String name() {
        return "Arr: " + type + "[] " + name;
    }
}