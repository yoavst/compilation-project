package ast.statements;

import ast.variables.AST_VAR;
import ast.expressions.AST_EXP;

/**
 * var := exp
 */
public class AST_STMT_ASSIGN extends AST_STMT {
    public AST_VAR var;
    public AST_EXP exp;

    public AST_STMT_ASSIGN(AST_VAR var, AST_EXP exp) {
        this.var = var;
        this.exp = exp;
    }

    @Override
    protected String name() {
        return ":=";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(exp);
    }
}
