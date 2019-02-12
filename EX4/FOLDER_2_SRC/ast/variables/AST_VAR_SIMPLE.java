package ast.variables;

import ir.IRContext;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.Operation;
import ir.memory.IRLoadCommand;
import ir.registers.Register;
import ir.registers.ThisRegister;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_VAR_SIMPLE extends AST_VAR {
    @NotNull
    public String name;

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
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        assert symbol != null;

        if (symbol.isBounded() && symbol.instance != null) {
            int fieldOffset = context.getFieldsTable(symbol.instance).get(symbol);
            Register thisReg = ThisRegister.instance;
            Register temp = context.getNewRegister();
            context.addCommand(new IRBinOpRightConstCommand(temp, thisReg, Operation.Plus, fieldOffset)); // temp hold address of variable
            context.addCommand(new IRLoadCommand(temp, temp)); // instance hold variable
            return temp;
        } else {
            // global or local variable
            //FIXME
        }
        return null;
    }
}
