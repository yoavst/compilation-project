package ast.expressions;

import ast.variables.AST_VAR;

public class AST_EXP_FUNC_CALL extends AST_EXP {
    public AST_VAR var;
    public String id;
    public AST_EXP_LIST expressions;

    public AST_EXP_FUNC_CALL(String id, AST_VAR var, AST_EXP_LIST expressions) {
        this.var = var;
        this.id = id;
        this.expressions = expressions;
    }

    public AST_EXP_FUNC_CALL(String id, AST_VAR var) {
        this(id, var, null);
    }

    public AST_EXP_FUNC_CALL(String id, AST_EXP_LIST expressions) {
        this(id, null, expressions);
    }

    public AST_EXP_FUNC_CALL(String id) {
        this(id, null, null);
    }

    @Override
    protected String name() {
        if (var != null)
            return "{VAR}." + id + "(...)";
        return id + "(...)";

    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(expressions);
    }
}
