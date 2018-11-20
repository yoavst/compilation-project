package ast;


import utils.Flags;
import utils.Graphwiz;
import utils.Printable;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class AST_Node implements Printable {
    private static int SerialNumberSeed = 0;

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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + name() + "]";
    }

    protected String name() {
        return "Unknown node";
    }

    public final void printAndEdge(AST_Node to) {
        if (to != null) {
            to.printMe();
            addEdge(this, to);
        }
    }

    protected final void addWrapperNode(final String name, Consumer<AST_Node> body) {
        printAndEdge(new AST_Node() {
            @Override
            public void printMe() {
                super.printMe();
                body.accept(this);
            }

            @Override
            protected String name() {
                return name;
            }
        });
    }

    private static void addNode(AST_Node node) {
        Graphwiz.getInstance().logNode(
                node.serialNumber,
                node.name());
    }

    private static void addEdge(AST_Node from, AST_Node to) {
        Graphwiz.getInstance().logEdge(from.serialNumber, to.serialNumber);
    }

    private static void println(String text) {
        if (Flags.DEBUG)
            System.out.println(text);
    }
}
