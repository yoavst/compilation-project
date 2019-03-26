package ast;


import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import types.Type;
import utils.*;
import utils.errors.SemanticException;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AST_Node implements Printable {
    private static int SerialNumberSeed = 0;
    public int lineNumber;

    /**
     * The serial number is for debug purposes.
     * In particular, it can help in creating a graphviz dot format of the AST.
     */
    private final int serialNumber = (SerialNumberSeed++);

    @Override
    public void printMe() {
        println("<[" + lineNumber + "] " + name() + ">");
        addNode(this);
    }

    /**
     * Run a semantic analysis on the node and its children.
     * Wrap the inner {@link #semantMe(SymbolTable)} call and check for {@link #errorReportable()}.
     *
     * @throws SemanticException on error
     */
    public final void semant(SymbolTable symbolTable) throws SemanticException {
        try {
            semantMe(symbolTable);
        } catch (SemanticException exception) {
            if (exception.getNode().errorReportable() || !errorReportable()) {
                // the exception was thrown by a node with permission to throw.
                // or, this node is not allowed to throw exceptions.
                throw exception;
            } else {
                SemanticException wrappedException = new SemanticException(this, "Wrapping error by "+ getClass().getSimpleName() +"[" + name() + "]");
                wrappedException.addSuppressed(exception);
                throw wrappedException;
            }
        }
    }

    /**
     * Run a semantic analysis on the node and its children.
     * <br>
     * <b>Note</b>:</b> Ignores {@link #errorReportable()}. Therefore, should call {@link #semant(SymbolTable)} on children.
     *
     * @throws SemanticException on error
     */
    protected abstract void semantMe(SymbolTable symbolTable) throws SemanticException;

    /**
     * Whether or not an error can be reported directly on this node.
     * <br><br>
     * For example:                 <br>
     * 1 string s = "helloworld"';  <br>
     * 2 int x = s.                 <br>
     * 3           test();          <br>
     * >> ERROR(2)                  <br>
     * <p>
     * Then, {@link ast.expressions.AST_EXP} should not be error reportable, but {@link ast.statements.AST_STMT}.
     * <br><br>
     * <b>Note:</b> The default behavior is not to be reportable.
     * <p>
     * For simplicity, here is the list of classes with error reporting:
     * <ul>
     * <li>{@link ast.declarations.AST_DEC}</li>
     * <li>{@link ast.statements.AST_STMT}</li>
     * </ul>
     */
    public boolean errorReportable() {
        return false;
    }

    /**
     * Convert this node and its children to IR form.
     */
    @NotNull
    public abstract Register irMe(IRContext context);

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name() + "]";
    }

    @NotNull
    protected String name() {
        return "Unknown node";
    }

    public Type getType() {
        return null;
    }

    //region AST printing utils
    protected final void printAndEdge(@Nullable AST_Node to) {
        if (to != null) {
            to.printMe();
            addEdge(this, to);
        }
    }

    protected final void addNodeUnderWrapper(@NotNull String name, @Nullable AST_Node node) {
        if (node != null)
            addListUnderWrapper(name, Collections.singletonList(node));
    }

    protected final void addListUnderWrapper(@NotNull String name, @NotNull List<? extends AST_Node> nodes) {
        if (!nodes.isEmpty()) {
            addWrapperNode(this, name, wrapperNode -> {
                for (AST_Node node : nodes) {
                    wrapperNode.printAndEdge(node);
                }
            });
        }
    }

    protected final void addListUnderWrapper(@NotNull String name, @NotNull AST_Node[] nodes) {
        if (nodes.length != 0) {
            addWrapperNode(this, name, wrapperNode -> {
                for (AST_Node node : nodes) {
                    wrapperNode.printAndEdge(node);
                }
            });
        }
    }

    private static void addWrapperNode(@NotNull AST_Node node, @NotNull final String name, @NotNull Consumer<AST_Node> body) {
        node.printAndEdge(new AST_Node() {
            @Override
            public void printMe() {
                super.printMe();
                body.accept(this);
            }

            @NotNull
            @Override
            protected String name() {
                return name;
            }

            @Override
            public void semantMe(SymbolTable symbolTable) {
                // just for printing, not a real node.
            }

            @Override
            public @NotNull Register irMe(IRContext context) {
                throw new IllegalStateException("should not be called");
            }
        });
    }

    private static void addNode(@NotNull AST_Node node) {
        Graphwiz.getInstance().logNode(
                node.serialNumber,
                node.name() + (node.getType() != null ? "\n" + node.getType() : ""));
    }

    private static void addEdge(@NotNull AST_Node from, @NotNull AST_Node to) {
        Graphwiz.getInstance().logEdge(from.serialNumber, to.serialNumber);
    }

    private static void println(@NotNull String text) {
        System.out.println(text);
    }
    //endregion

    protected final void throwSemantic(String reason) throws SemanticException {
        throw new SemanticException(this, reason);
    }
}
