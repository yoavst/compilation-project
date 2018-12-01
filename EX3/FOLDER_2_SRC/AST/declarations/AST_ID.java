package ast.declarations;

import ast.AST_Node;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.SemanticException;

public class AST_ID extends AST_Node {
    @NotNull
    public String type;
    @NotNull
    public String name;

    private Type typing;

    public AST_ID(@NotNull String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "[" + type + " " + name + "]";
    }

    /**
     * After {@link #semantMe(SymbolTable)} has run successfully, the type info of the parameter should be available using this method.
     * @return The type info of the expression
     *
     * @throws IllegalStateException if {@link #semantMe(SymbolTable)} has yet to be run.
     */
    @NotNull
    @Override
    public Type getType() {
        if (typing == null) {
            throw new IllegalStateException("Type info is unavailable. Possible solution: run semantMe().");
        }
        return typing;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        typing = symbolTable.findGeneralizedType(type);
        if (typing == TypeVoid.instance) {
            throwSemantic("Trying to declare a function with a void parameter");
        } else if (typing == null) {
            throwSemantic("Trying to declare a function with a parameter of unknown type");
        }
    }
}
