package ast.declarations;

import ast.AST_Node;
import symbols.SymbolTable;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public abstract class AST_DEC extends AST_Node {
    @Nullable
    public String type;
    @NotNull
    public String name;

    public AST_DEC(@Nullable String type, @NotNull String name) {
        this.type = type;
        this.name = name;
    }

    @NotNull
    @Override
    protected String name() {
        return "Unknown DEC";
    }

    @Override
    public boolean errorReportable() {
        return true;
    }

    /**
     * Semant only the header, allowing the enclosing scope to know about its children out of order
     */
    public abstract void semantHeader(SymbolTable symbolTable) throws SemanticException;
}