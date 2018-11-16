package ast.statements;

import ast.expressions.AST_EXP_LIST;
import ast.variables.AST_VAR;

public class AST_STMT_FUNC_CALL extends AST_STMT {
    public AST_VAR var;
    public String id;
    public AST_EXP_LIST expressions;


    public AST_STMT_FUNC_CALL(String id, AST_EXP_LIST expressions, AST_VAR var) {
        this.var = var;
        this.id = id;
        this.expressions = expressions;
    }

    public AST_STMT_FUNC_CALL(String id, AST_EXP_LIST expressions) {
        this(id, expressions, null);
    }

    public AST_STMT_FUNC_CALL(String id, AST_VAR var) {
        this(id, null, var);
    }


    public AST_STMT_FUNC_CALL(String id) {
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
