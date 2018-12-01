package ast.declarations;

import symbols.SymbolTable;
import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.ClassScan;
import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Class;
import types.TypeClass;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_DEC_CLASS extends AST_DEC {
    @Nullable
    public String parentClass;
    @NotNull
    public AST_DEC[] fields;

    public TypeClass representingType;

    public AST_DEC_CLASS(@NotNull String name, @NotNull AST_DEC[] fields, @Nullable String parentClass) {
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
        symbolTable.beginScope(ClassScan, representingType);
        SemanticException headerException = null;
        AST_DEC exceptionField = null;
        for (AST_DEC field : fields) {
            try {
                field.semantHeader(symbolTable);
            } catch (SemanticException e) {
                if (headerException == null) {
                    headerException = e;
                    exceptionField = field;
                }
            }
        }
        symbolTable.endScope();
        // Since it is a class scan scope, the symbol table will merge all the inner declarations into the class type

        // now, we need to semant every function. However, if we add an exception earlier,
        // we'll have to report it on the right time, because we need to report the first error.
        symbolTable.beginScope(Class, representingType);
        for (AST_DEC field : fields) {
            if (headerException != null && exceptionField == field) {
                throw headerException;
            }
            field.semant(symbolTable);
        }
        symbolTable.endScope();
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        TypeClass parent = null;
        if (parentClass != null) {
            parent = symbolTable.findClassType(parentClass);
            if (parent == null) {
                throwSemantic("Trying to declare a class extending unknown class type: <" + parentClass + ">");
            }
        }
        representingType = new TypeClass(name, parent);

        // TODO check if not in scope yet
        symbolTable.enter(name, representingType);
    }
}