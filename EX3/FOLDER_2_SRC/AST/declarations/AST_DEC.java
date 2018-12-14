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
    protected abstract void semantMeHeader(SymbolTable symbolTable) throws SemanticException;

    /**
     * Semant only the header, allowing the enclosing scope to know about its children out of order
     * Wrap the inner {@link #semantMe(SymbolTable)} call and check for {@link #errorReportable()}.
     *
     * @throws SemanticException on error
     */
    public final void semantHeader(SymbolTable symbolTable) throws SemanticException {
        try {
            semantMeHeader(symbolTable);
        } catch (SemanticException exception) {
            if (exception.getNode().errorReportable()) {
                // the exception was thrown by a node with permission to throw.
                throw exception;
            } else {
                SemanticException wrappedException = new SemanticException(this, "Wrapping error: " + exception.getReason());
                wrappedException.addSuppressed(exception);
                throw wrappedException;
            }
        }
    }
}