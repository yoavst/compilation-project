package ast.statements;

import ast.expressions.AST_EXP_FUNC_CALL;
import ir.utils.IRContext;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_STMT_FUNC_CALL extends AST_STMT {
    @NotNull
    public AST_EXP_FUNC_CALL funcCall;

    public AST_STMT_FUNC_CALL(@NotNull AST_EXP_FUNC_CALL funcCall) {
        this.funcCall = funcCall;
    }

    @NotNull
    @Override
    protected String name() {
        return "Func call";

    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(funcCall);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        funcCall.semant(symbolTable);
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        return funcCall.irMe(context);
    }
}

