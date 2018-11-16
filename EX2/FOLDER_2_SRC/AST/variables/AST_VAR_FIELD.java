package ast.variables;

public class AST_VAR_FIELD extends AST_VAR {
    public AST_VAR var;
    public String fieldName;

    public AST_VAR_FIELD(AST_VAR var, String fieldName) {
        this.var = var;
        this.fieldName = fieldName;
    }

    @Override
    protected String name() {
        return "VAR(_." + fieldName + ")";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }
}
