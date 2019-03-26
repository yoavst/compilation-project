package ast.variables;

import ir.commands.memory.IRStoreCommand;
import ir.utils.IRContext;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRSetValueCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.memory.IRLoadCommand;
import ir.registers.Register;
import ir.registers.ThisRegister;
import symbols.Symbol;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.function.Supplier;

public class AST_VAR_SIMPLE extends AST_VAR {
    @NotNull
    public String name;
    private Symbol symbol;

    public AST_VAR_SIMPLE(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(" + name + ")";
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        symbol = symbolTable.findField(name, false);
        if (symbol == null) {
            throwSemantic("Trying to access non-existent field: \"" + name + "\"");
        }
        type = symbol.type;
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        assert symbol != null;

        if (symbol.isBounded() && symbol.instance != null) {
            int fieldOffset = context.getFieldOffset(symbol);
            Register thisReg = ThisRegister.instance;
            Register temp1 = context.newRegister();
            context.command(new IRBinOpRightConstCommand(temp1, thisReg, Operation.Plus, fieldOffset)); // temp1 hold address of variable
            Register temp2 = context.newRegister();
            context.command(new IRLoadCommand(temp2, temp1)); // temp2 hold variable
            return temp2;
        } else {
            // unbounded variable
            Register temp = context.newRegister();
            context.command(new IRSetValueCommand(temp, context.registerFor(symbol)));
            return temp;
        }
    }

    @Override
    public void irAssignTo(IRContext context, Supplier<Register> data) {
        assert symbol != null;

        Register content = data.get();


        if (symbol.isBounded() && symbol.instance != null) {
            int fieldOffset = context.getFieldOffset(symbol);
            Register thisReg = ThisRegister.instance;
            Register temp1 = context.newRegister();
            context.command(new IRBinOpRightConstCommand(temp1, thisReg, Operation.Plus, fieldOffset)); // temp1 hold address of variable
            context.command(new IRStoreCommand(temp1, content));

        } else {
            // unbounded variable
            context.command(new IRSetValueCommand(context.registerFor(symbol), content));
        }
    }
}
