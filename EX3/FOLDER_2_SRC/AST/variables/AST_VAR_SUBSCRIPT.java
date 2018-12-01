package ast.variables;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.SemanticException;

public class AST_VAR_SUBSCRIPT extends AST_VAR {
    @NotNull
    public AST_VAR var;
    @NotNull
    public AST_EXP subscript;

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

        assert var.type != null;
        if (!var.type.isArray()) {
            throwSemantic("Trying to do var[...] on non-array type: " + var.type);
        } else if (subscript.getType() != TypeInt.instance) {
            throwSemantic("Trying to do var[...] with non-integral index: " + subscript.getType());
        }

        type = ((TypeArray) var.type).arrayType;
    }
}
