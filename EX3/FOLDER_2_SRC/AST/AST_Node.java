package ast;


import symbols.SymbolTable;
import utils.*;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public abstract class AST_Node implements Printable {
    private static int SerialNumberSeed = 0;
    private int lineNumber;

    /**
     * The serial number is for debug purposes.
     * In particular, it can help in creating a graphviz dot format of the AST.
     */
    private final int serialNumber = (SerialNumberSeed++);

    @Override
    public void printMe() {
        println("<" + name() + ">");
        addNode(this);
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    /**
     * Run a semantic analysis on the node and its children.
     *
     * @throws SemanticException on error
     */
    public void semantMe(SymbolTable symbolTable) throws SemanticException {
        // FIXME change to abstract method: it's not abstract just to make it compile
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name() + "]";
    }

    @NotNull
    protected String name() {
        return "Unknown node";
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
        });
    }

    private static void addNode(@NotNull AST_Node node) {
        Graphwiz.getInstance().logNode(
                node.serialNumber,
                node.name());
    }

    private static void addEdge(@NotNull AST_Node from, @NotNull AST_Node to) {
        Graphwiz.getInstance().logEdge(from.serialNumber, to.serialNumber);
    }

    private static void println(@NotNull String text) {
        if (Flags.DEBUG)
            System.out.println(text);
    }
    //endregion
}
