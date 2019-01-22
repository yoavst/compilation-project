package ast.variables;

import ast.AST_Node;
import symbols.SymbolTable;
import types.Type;
import utils.NotNull;
import utils.Nullable;

public abstract class AST_VAR extends AST_Node {
    @Nullable
    protected Type type;

    @NotNull
    @Override
    protected String name() {
        return "unknown Var";
    }

    /**
     * After {@link #semantMe(SymbolTable)} has run successfully, the type info of the variable should be available using this method.
     * @return The type info of the expression
     *
     * @throws IllegalStateException if {@link #semantMe(SymbolTable)} has yet to be run.
     */
    @NotNull
    @Override
    public Type getType() {
        if (type == null) {
            throw new IllegalStateException("Type info is unavailable. Possible solution: run semantMe().");
        }
        return type;
    }
}
