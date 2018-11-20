package ast.expressions;

import ast.variables.AST_VAR;

import java.util.Collections;
import java.util.List;

public class AST_EXP_FUNC_CALL extends AST_EXP {
    public AST_VAR var;
    public String id;
    public List<AST_EXP> funcParameters;

    public AST_EXP_FUNC_CALL(String id, AST_VAR var, List<AST_EXP> funcParameters) {
        this.var = var;
        this.id = id;
        this.funcParameters = funcParameters;
    }

    public AST_EXP_FUNC_CALL(String id, AST_VAR var) {
        this(id, var, Collections.emptyList());
    }

    public AST_EXP_FUNC_CALL(String id, List<AST_EXP> funcParameters) {
        this(id, null, funcParameters);
    }

    public AST_EXP_FUNC_CALL(String id) {
        this(id, null, Collections.emptyList());
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
        addListUnderWrapper("parameters", funcParameters);
    }
}
