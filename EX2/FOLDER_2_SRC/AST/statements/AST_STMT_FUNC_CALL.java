package ast.statements;

import ast.expressions.AST_EXP;
import ast.variables.AST_VAR;
import utils.NotNull;
import utils.Nullable;

import java.util.Collections;
import java.util.List;

public class AST_STMT_FUNC_CALL extends AST_STMT {
    @Nullable
    public AST_VAR var;
    @NotNull
    public String id;
    @NotNull
    public List<AST_EXP> expressions;


    public AST_STMT_FUNC_CALL(@NotNull String id, @NotNull List<AST_EXP> expressions, @Nullable AST_VAR var) {
        this.var = var;
        this.id = id;
        this.expressions = expressions;
    }

    public AST_STMT_FUNC_CALL(@NotNull String id, @NotNull List<AST_EXP> expressions) {
        this(id, expressions, null);
    }

    public AST_STMT_FUNC_CALL(@NotNull String id, @Nullable AST_VAR var) {
        this(id, Collections.emptyList(), var);
    }


    public AST_STMT_FUNC_CALL(@NotNull String id) {
        this(id, Collections.emptyList(), null);
    }

    @NotNull
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
        addListUnderWrapper("parameters", expressions);
    }
}
