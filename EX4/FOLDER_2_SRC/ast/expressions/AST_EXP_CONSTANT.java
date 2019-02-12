package ast.expressions;

import symbols.SymbolTable;
import types.Type;

public abstract class AST_EXP_CONSTANT extends AST_EXP {
    AST_EXP_CONSTANT(Type type) {
        this.type = type;
    }

    @Override
    public void semantMe(SymbolTable symbolTable) {
        // NO-OP since type info is already available
    }
}
