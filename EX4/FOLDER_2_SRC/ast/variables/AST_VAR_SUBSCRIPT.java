package ast.variables;

import ast.expressions.AST_EXP;
import ir.IRContext;
import ir.arithmetic.IRBinOpCommand;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.Operation;
import ir.memory.IRLoadCommand;
import ir.registers.Register;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_VAR_SUBSCRIPT extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    private AST_EXP subscript;

    public AST_VAR_SUBSCRIPT(@NotNull AST_VAR var, @NotNull AST_EXP subscript) {
        this.var = var;
        this.subscript = subscript;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(_.[...])";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
        printAndEdge(subscript);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semant(symbolTable);
        subscript.semant(symbolTable);

        if (!var.getType().isArray()) {
            throwSemantic("Trying to do var[...] on non-array type: " + var.getType());
        } else if (subscript.getType() != TypeInt.instance) {
            throwSemantic("Trying to do var[...] with non-integral index: " + subscript.getType());
        }

        symbol = var.symbol;
    }

    @NotNull
    @Override
    public Type getType() {
        if (symbol == null) {
            throw new IllegalStateException("Type info is unavailable. Possible solution: run semantMe().");
        }
        return ((TypeArray) symbol.type).arrayType;
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        assert symbol != null && symbol.instance != null;

        Register instance = var.irMe(context);
        Register index = subscript.irMe(context);
        Register temp = context.getNewRegister();
        context.addCommand(new IRBinOpRightConstCommand(index, index, Operation.Times, ((TypeArray) var.getType()).arrayType.size()));
        context.addCommand(new IRBinOpCommand(index, instance, Operation.Plus, index));
        context.addCommand(new IRLoadCommand(temp, index)); // instance hold variable
        context.freeRegister(instance);
        context.freeRegister(index);
        return temp;
    }
}
