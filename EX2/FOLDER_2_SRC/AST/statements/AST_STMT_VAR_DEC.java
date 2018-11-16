package ast.statements;

import ast.declarations.AST_DEC_VAR;

public class AST_STMT_VAR_DEC extends AST_STMT {
    public AST_DEC_VAR var;

    public AST_STMT_VAR_DEC(AST_DEC_VAR var) {
        this.var = var;
    }

    @Override
    protected String name() {
        return "Stmt var dec";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }


}
