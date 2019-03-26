package ast.expressions;

import ast.AST_Node;
import symbols.SymbolTable;
import types.Type;
import utils.NotNull;
import utils.Nullable;

public abstract class AST_EXP extends AST_Node {
    @Nullable
    protected Type type;

    @NotNull
    @Override
    protected String name() {
        return "Unknown Exp";
    }

    /**
     * After {@link #semantMe(SymbolTable)} has run successfully, the type info of the expression should be available using this method.
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

    boolean isConst() {
        return false;
    }

    Object getConstValue() {
        return null;
    }
}