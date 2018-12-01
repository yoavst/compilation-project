package ast.variables;

import symbols.SymbolTable;
import types.Type;
import types.TypeClass;
import utils.NotNull;
import utils.SemanticException;

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

        Type fieldType = ((TypeClass) var.getType()).queryFieldRecursively(fieldName);
        if (fieldType == null) {
            throwSemantic("Trying to access the non-existent field \"" + fieldName + "\" on type: " + var.getType());
        }

        type = fieldType;
    }
}
