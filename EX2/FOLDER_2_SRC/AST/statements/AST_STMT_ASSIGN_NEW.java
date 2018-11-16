package ast.statements;

import ast.variables.AST_VAR;
import ast.expressions.AST_NEW_EXP;

public class AST_STMT_ASSIGN_NEW extends AST_STMT {
    public AST_VAR var;
    public AST_NEW_EXP exp;

    public AST_STMT_ASSIGN_NEW(AST_VAR var, AST_NEW_EXP exp) {
        this.var = var;
        this.exp = exp;
    }

    @Override
    protected String name() {
        return ":= new";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(exp);
    }


}
