package ast.variables;

import ast.expressions.AST_EXP;
import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.memory.IRLoadCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.function.Supplier;

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

        type = ((TypeArray) var.getType()).arrayType;
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        Register instance = var.irMe(context);
        context.checkNotNullForArray(instance);
        Register index = subscript.irMe(context);
        context.checkLength(instance, index);

        Register temp = context.newRegister();
        context.command(new IRBinOpRightConstCommand(temp, index, Operation.Times, IRContext.PRIMITIVE_DATA_SIZE)); // temp = index * sizeof(element)
        context.command(new IRBinOpCommand(temp, instance, Operation.Plus, temp)); // temp = instance + temp
        context.command(new IRBinOpRightConstCommand(temp, temp, Operation.Plus, IRContext.ARRAY_DATA_INITIAL_OFFSET)); // temp = temp + initial offset

        Register temp2 = context.newRegister();
        context.command(new IRLoadCommand(temp2, temp)); // instance hold variable
        return temp2;
    }

    @Override
    public void irAssignTo(IRContext context, Supplier<Register> data) {
        Register instance = var.irMe(context);
        context.checkNotNullForArray(instance);
        Register index = subscript.irMe(context);
        context.checkLength(instance, index);

        Register content = data.get();

        Register temp = context.newRegister();
        context.command(new IRBinOpRightConstCommand(temp, index, Operation.Times, IRContext.PRIMITIVE_DATA_SIZE)); // temp = index * sizeof(element)
        context.command(new IRBinOpCommand(temp, instance, Operation.Plus, temp)); // temp = instance + temp
        context.command(new IRBinOpRightConstCommand(temp, temp, Operation.Plus, IRContext.ARRAY_DATA_INITIAL_OFFSET)); // temp = temp + initial offset

        context.command(new IRStoreCommand(temp, content));
    }
}
