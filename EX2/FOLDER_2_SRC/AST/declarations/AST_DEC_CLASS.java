package ast.declarations;

import java.util.List;

public class AST_DEC_CLASS extends AST_DEC {
    public String parentClass;
    public List<AST_DEC> fields;

    public AST_DEC_CLASS(String name, List<AST_DEC> fields, String parentClass) {
        super(null, name);

        this.fields = fields;
        this.parentClass = parentClass;
    }

    public AST_DEC_CLASS(String name, List<AST_DEC> fields) {
        this(name, fields, null);
    }


    @Override
    protected String name() {
        return "Class " + name + (parentClass == null ? "" : ("extends " + parentClass));
    }

    @Override
    public void printMe() {
        super.printMe();
        for (AST_DEC declaration : fields) {
            printAndEdge(declaration);
        }
    }
}