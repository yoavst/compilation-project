package ast.declarations;

import symbols.SymbolTable;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

import java.util.List;

public class AST_DEC_CLASS extends AST_DEC {
    @Nullable
    public String parentClass;
    @NotNull
    public AST_DEC[] fields;

    public AST_DEC_CLASS(@NotNull String name, @NotNull  AST_DEC[] fields, @Nullable String parentClass) {
        super(null, name);

        this.fields = fields;
        this.parentClass = parentClass;
    }

    public AST_DEC_CLASS(@NotNull String name, @NotNull AST_DEC[] fields) {
        this(name, fields, null);
    }


    @NotNull
    @Override
    protected String name() {
        return "Class " + name + (parentClass == null ? "" : ("extends " + parentClass));
    }

    @Override
    public void printMe() {
        super.printMe();
        addListUnderWrapper("body", fields);
    }

    @Override
    public void semantMe(SymbolTable symbolTable) throws SemanticException {
        super.semantMe(symbolTable);

    }
}