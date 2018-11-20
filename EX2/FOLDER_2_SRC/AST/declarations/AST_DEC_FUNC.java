package ast.declarations;

import ast.statements.AST_STMT_LIST;

import java.util.Collections;
import java.util.List;

public class AST_DEC_FUNC extends AST_DEC {
    public List<AST_ID> parameters;
    public AST_STMT_LIST statements;

    public AST_DEC_FUNC(String type, String name, AST_STMT_LIST statements, List<AST_ID> parameters) {
        super(type, name);

        this.parameters = parameters;
        this.statements = statements;
    }

    public AST_DEC_FUNC(String type, String name, AST_STMT_LIST statements) {
        this(type, name, statements, Collections.emptyList());
    }

    @Override
    protected String name() {
        return "fun " + name + "(...): " + type;
    }

    @Override
    public void printMe() {
        super.printMe();

        if (!parameters.isEmpty()) {
            addWrapperNode("parameters", ast_node -> {
                for (AST_ID var : parameters) {
                    ast_node.printAndEdge(var);
                }
            });
        }

        printAndEdge(statements);
    }
}