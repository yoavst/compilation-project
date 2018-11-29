package ast.variables;

import utils.NotNull;

public class AST_VAR_FIELD extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    public String fieldName;

    public AST_VAR_FIELD(@NotNull AST_VAR var, @NotNull String fieldName) {
        this.var = var;
        this.fieldName = fieldName;
    }

    @NotNull
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
