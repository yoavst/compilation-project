package ast.expressions;

import ast.variables.AST_VAR;
import utils.NotNull;
import utils.Nullable;

import java.util.Collections;
import java.util.List;

public class AST_EXP_FUNC_CALL extends AST_EXP {
    @Nullable
    public AST_VAR var;
    @NotNull
    public String id;
    @NotNull
    public List<AST_EXP> funcParameters;

    public AST_EXP_FUNC_CALL(@NotNull String id, @Nullable AST_VAR var, @NotNull List<AST_EXP> funcParameters) {
        this.var = var;
        this.id = id;
        this.funcParameters = funcParameters;
    }

    public AST_EXP_FUNC_CALL(@NotNull String id,@Nullable AST_VAR var) {
        this(id, var, Collections.emptyList());
    }

    public AST_EXP_FUNC_CALL(@NotNull String id, @NotNull List<AST_EXP> funcParameters) {
        this(id, null, funcParameters);
    }

    public AST_EXP_FUNC_CALL(@NotNull String id) {
        this(id, null, Collections.emptyList());
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
        addNodeUnderWrapper("callee", var);
        addListUnderWrapper("parameters", funcParameters);
    }
}
