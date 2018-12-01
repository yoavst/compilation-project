package ast.declarations;

import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeArray;
import utils.NotNull;
import utils.SemanticException;

public class AST_DEC_ARRAY extends AST_DEC {
    public AST_DEC_ARRAY(String type, String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Arr: " + type + "[] " + name;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
       // no-op since just an header
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        Type arrayType = symbolTable.findGeneralizedType(name);
        if (arrayType == null) {
            throwSemantic("Trying to declare an array type of an unknown type: \"" + type + "\"");
        }

        // check scoping rules
        if (symbolTable.find(name) != null) {
            throwSemantic("Trying to declare an array typedef but the name \"" + name + "\" is already in use");
        }
        symbolTable.enter(name, new TypeArray(name, arrayType));
    }
}