package ast.declarations;

import symbols.SymbolTable;
import types.TypeError;
import utils.NotNull;

public abstract class AST_DEC_VAR extends AST_DEC {
    public AST_DEC_VAR(@NotNull String type, @NotNull String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Unknown var Dec";
    }

    /**
     * Returns true if the variable is allowed to be defined with the given name in the current scope.
     */
    public static boolean canBeDefined(SymbolTable symbolTable, String name) {
        /*
         * section 3.7:
         *   global function/variable:
         *     different than defined types, functions, (assuming also variables)
         *   member function/variables:
         *     if parent had function/variable with same name - invalid.
         *     unless it is a function override
         * table 10:
         *   shadowing a parent class member is invalid.
         *   shadowing a function parameter is invalid.
         *   shadowing a global variable with a class member is valid
         *   shadowing a variable from outside a function is valid.
         */
        if (symbolTable.findGeneralizedType(name) != null) {
            return false;
        } else if (symbolTable.getEnclosingFunction() != null) {
            return symbolTable.findInCurrentScope(name) == null;
        } else if (symbolTable.getEnclosingClass() != null) {
            return symbolTable.getEnclosingClass().queryFieldRecursively(name) == null && symbolTable.findInCurrentScope(name) == null;
        } else {
            return symbolTable.find(name) == null;
        }
    }
}