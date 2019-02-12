package ast.expressions;

import ast.variables.AST_VAR;
import ir.utils.IRContext;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.Operation;
import ir.flow.IRLabel;
import ir.functions.IRCallCommand;
import ir.functions.IRCallRegisterCommand;
import ir.functions.IRPopCommand;
import ir.functions.IRPushCommand;
import ir.memory.IRLoadCommand;
import ir.registers.Register;
import symbols.Symbol;
import symbols.SymbolTable;
import types.Type;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeNil;
import utils.NotNull;
import utils.Nullable;
import utils.errors.ParameterMismatchFunctionCallException;
import utils.errors.SemanticException;
import utils.errors.UndefinedFunctionCallException;

import java.util.Collections;
import java.util.List;

public class AST_EXP_FUNC_CALL extends AST_EXP {
    @Nullable
    public AST_VAR var;
    @NotNull
    public String id;
    @NotNull
    private List<AST_EXP> funcParameters;
    private Symbol symbol;

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
                symbol = ((TypeClass) callingType).queryMethodRecursively(id);
                if (symbol == null) {
                    throwSemantic("Trying to call a non-existent member function: " + callingType + "." + id + "(...)");
                } else
                    checkFunctionCall(symbol.getFunction(), "Trying to call member function " + callingType + "." + id);
            }
        } else {
            // first, check in the class scope
            final TypeClass innerClass = symbolTable.getEnclosingClass();
            if (innerClass != null) {
                symbol = innerClass.queryMethodRecursively(id);
            }
            // then, check if it in the global scope, skipping class scope
            if (symbol == null) {
                symbol = symbolTable.findMethod(id, true);
            }

            if (symbol == null) {
                throw new UndefinedFunctionCallException(this, id);
            } else checkFunctionCall(symbol.getFunction(), "Trying to call function " + id + "(...)");
        }
    }

    /**
     * Check if the call is valid
     * Updates {@link #type} when done.
     *
     * @param initialErrorText A prefix that will be added to the error text
     */
    private void checkFunctionCall(@NotNull TypeFunction function, String initialErrorText) throws SemanticException {
        if (function.params.size() != funcParameters.size()) {
            throw new ParameterMismatchFunctionCallException(this,
                    initialErrorText + " with invalid size of parameters. Expected: " + function.params.size() + ". Received: " + funcParameters.size() + ".");
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

                    throw new ParameterMismatchFunctionCallException(this,
                            initialErrorText + ". Expected parameter " + i + " to be " + expected + " received NIL instead.");
                } else if (!expected.isAssignableFrom(given.type)) {
                    throw new ParameterMismatchFunctionCallException(this,
                            initialErrorText + ". Expected parameter " + i + " to be " + expected + " received " + given.type + " instead.");
                }
            }

            type = function.returnType;
        }
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        // [this] is the first parameter for instance function
        Register instance = var != null ? var.irMe(context) : null;
        if (instance != null) {
            context.checkNotNull(instance);
            context.command(new IRPushCommand(instance));
        }

        // push parameters
        for (AST_EXP funcParameter : funcParameters) {
            Register param = funcParameter.irMe(context);
            context.command(new IRPushCommand(param));
        }

        // call function
        if (symbol.isBounded() && instance != null) {
            // bounded function
            int virtualTableIndex = context.getMethodOffsetInVtable(symbol);
            Register temp1 = context.newRegister();
            context.command(new IRBinOpRightConstCommand(temp1, instance, Operation.Plus, IRContext.VIRTUAL_TABLE_OFFSET_IN_OBJECT)); // temp1 = instance_addr + offset_for_vtable
            Register temp2 = context.newRegister();
            context.command(new IRLoadCommand(temp2, temp1)); // temp2 = *temp1
            context.command(new IRBinOpRightConstCommand(temp2, temp2, Operation.Plus, virtualTableIndex)); // temp2 += offset
            Register temp3 = context.newRegister();
            context.command(new IRLoadCommand(temp3, temp2)); // temp3 = *temp2
            context.command(new IRCallRegisterCommand(temp3)); // call temp3
        } else {
            // global function
            IRLabel label = context.functionLabelFor(symbol);
            context.command(new IRCallCommand(label));
        }
        Register temp = context.newRegister();
        context.command(new IRPopCommand(temp));
        return temp;
    }
}
