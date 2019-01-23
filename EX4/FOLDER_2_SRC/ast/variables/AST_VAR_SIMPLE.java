package ast.variables;

import ir.IRContext;
import ir.Register;
import ir.memory.IRLoadVariableCommand;
import symbols.SymbolTable;
import types.Type;
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
        Type fieldType = symbolTable.findField(name, false);
        if (fieldType == null) {
            throwSemantic("Trying to access non-existent field: \"" + name + "\"");
        }

        type = fieldType;
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        Register temp = context.getNewRegister();
        context.addCommand(new IRLoadVariableCommand(temp, name));
        return temp;
    }
}
