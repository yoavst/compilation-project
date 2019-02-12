package ast.variables;

import ir.IRContext;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.Operation;
import ir.memory.IRLoadCommand;
import ir.registers.Register;
import symbols.SymbolTable;
import types.TypeClass;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_VAR_FIELD extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    public String fieldName;

    public AST_VAR_FIELD(@NotNull AST_VAR var, @NotNull String fieldName) {
        this.var = var;
        this.fieldName = fieldName;
    }

    @NotNull
    @Override
    protected String name() {
        return "VAR(_." + fieldName + ")";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(var);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        var.semant(symbolTable);
        if (!var.getType().isClass()) {
            throwSemantic("Trying to access the field \"" + fieldName + "\" on non-class type: " + var.getType());
        }

        symbol = ((TypeClass) var.getType()).queryFieldRecursively(fieldName);
        if (symbol == null) {
            throwSemantic("Trying to access the non-existent field \"" + fieldName + "\" on type: " + var.getType());
        }
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        assert symbol != null && symbol.instance != null;

        Register instance = var.irMe(context);
        int fieldOffset = context.getFieldsTable(symbol.instance).get(symbol);
        Register temp = context.getNewRegister();
        context.addCommand(new IRBinOpRightConstCommand(temp, instance, Operation.Plus, fieldOffset)); // temp hold address of variable
        context.addCommand(new IRLoadCommand(temp, temp)); // instance hold variable
        context.freeRegister(instance);
        return temp;
    }
}
