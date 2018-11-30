package ast.declarations;

import ast.statements.AST_STMT;
import utils.NotNull;

import java.util.Collections;
import java.util.List;

public class AST_DEC_FUNC extends AST_DEC {
    @NotNull
    public List<AST_ID> parameters;
    @NotNull
    public AST_STMT[] statements;

    public AST_DEC_FUNC(@NotNull String type, @NotNull String name, @NotNull AST_STMT[] statements, @NotNull List<AST_ID> parameters) {
        super(type, name);

        this.parameters = parameters;
        this.statements = statements;
    }

    public AST_DEC_FUNC(@NotNull String type, @NotNull String name, @NotNull AST_STMT[] statements) {
        this(type, name, statements, Collections.emptyList());
    }

    @NotNull
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