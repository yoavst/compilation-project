package ast.declarations;

public class AST_DEC_CLASS extends AST_DEC {
    public String parentClass;
    public AST_DEC_LIST fields;

    public AST_DEC_CLASS(String name, AST_DEC_LIST fields, String parentClass) {
        super(null, name);

        this.fields = fields;
        this.parentClass = parentClass;
    }

    public AST_DEC_CLASS(String name, AST_DEC_LIST fields) {
        this(name, fields, null);
    }


    @Override
    protected String name() {
        return "Class " + name + (parentClass == null ? "" : ("extends " + parentClass));
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(fields);
    }
}