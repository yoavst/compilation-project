package ast.expressions;

import ast.variables.AST_VAR;
import symbols.SymbolTable;
import types.Type;
import types.TypeClass;
import types.TypeFunction;
import types.TypeFunctionUnspecified;
import types.builtins.TypeNil;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

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

    public AST_EXP_FUNC_CALL(@NotNull String id, @Nullable AST_VAR var) {
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

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        if (var != null)
            var.semant(symbolTable);
        for (AST_EXP funcParameter : funcParameters) {
            funcParameter.semant(symbolTable);
        }

        if (var != null) {
            Type callingType = var.getType();
            if (!callingType.isClass()) {
                throwSemantic("Trying to make a function call on non class type: " + callingType);
            } else {
                TypeFunction function = ((TypeClass) callingType).queryMethodRecursively(id);
                if (function == null) {
                    throwSemantic("Trying to call a non-existent member function: " + callingType + "." + id + "(...)");
                } else checkFunctionCall(function, "Trying to call member function " + callingType + "." + id);
            }
        } else {
            TypeFunction function = null;
            // first, check in the class scope
            final TypeClass innerClass = symbolTable.getEnclosingClass();
            if (innerClass != null) {
                function = innerClass.queryMethodRecursively(id);
            }
            // then, check if it in the global scope, skipping class scope
            if (function == null) {
                function = symbolTable.findMethod(id, true);
            }

            if (function == null) {
                throwSemantic("Trying to call non-existent function: " + id + "(...)");
            } else checkFunctionCall(function, "Trying to call function " + id + "(...)");
        }
    }

    /**
     * Check if the call is valid
     * Updates {@link #type} when done.
     *
     * @param initialErrorText A prefix that will be added to the error text
     */
    private void checkFunctionCall(@NotNull TypeFunction function, String initialErrorText) throws SemanticException {
        if (function instanceof TypeFunctionUnspecified) {
            type = function.returnType;
            return;
        }

        if (function.params.size() != funcParameters.size()) {
            throwSemantic(initialErrorText + " with invalid size of parameters. Expected: " + function.params.size() + ". Received: " + funcParameters.size() + ".");
        } else {
            // check signature is the same
            for (int i = 0; i < funcParameters.size(); i++) {
                Type expected = function.params.get(i);
                AST_EXP given = funcParameters.get(i);

                if (given.type == TypeNil.instance) {
                    // according to 3.1.1 and 3.1.2 can pass nil instead of class or array
                    if (expected.isClass() || expected.isArray()) {
                        continue;
                    }

                    throwSemantic(initialErrorText + ". Expected parameter " + i + " to be " + expected + " received NIL instead.");
                } else if (!expected.isAssignableFrom(given.type)) {
                    throwSemantic(initialErrorText + ". Expected parameter " + i + " to be " + expected + " received " + given.type + " instead.");
                }
            }

            type = function.returnType;
        }
    }
}
