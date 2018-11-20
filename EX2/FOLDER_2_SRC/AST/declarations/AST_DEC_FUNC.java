package ast.declarations;

import ast.statements.AST_STMT;

import java.util.Collections;
import java.util.List;

public class AST_DEC_FUNC extends AST_DEC {
    public List<AST_ID> parameters;
    public List<AST_STMT> statements;

    public AST_DEC_FUNC(String type, String name, List<AST_STMT> statements, List<AST_ID> parameters) {
        super(type, name);

        this.parameters = parameters;
        this.statements = statements;
    }

    public AST_DEC_FUNC(String type, String name, List<AST_STMT> statements) {
        this(type, name, statements, Collections.emptyList());
    }

    @Override
    protected String name() {
        return "fun " + name + "(...): " + type;
    }

    @Override
    public void printMe() {
        super.printMe();

        addListUnderWrapper("parameters", parameters);
        addListUnderWrapper("body", statements);
    }
}