package ast.declarations;

import ast.statements.AST_STMT_LIST;

public class AST_DEC_FUNC extends AST_DEC {
    public AST_ID_LIST vars;
    public AST_STMT_LIST statements;

    public AST_DEC_FUNC(String type, String name, AST_STMT_LIST statements, AST_ID_LIST vars) {
        super(type, name);

        this.vars = vars;
        this.statements = statements;
    }

    public AST_DEC_FUNC(String type, String name, AST_STMT_LIST statements) {
        this(type, name, statements, null);
    }

    @Override
    protected String name() {
        return "fun " + name + "(...): " + type;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(vars);
        printAndEdge(statements);
    }
}