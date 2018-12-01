package ast.declarations;

import ast.expressions.AST_NEW_EXP;
import symbols.SymbolTable;
import types.Type;
import types.TypeClass;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_DEC_VAR_NEW extends AST_DEC_VAR {
    @NotNull
    public AST_NEW_EXP newExp;

    public Type representingType;

    public AST_DEC_VAR_NEW(@NotNull String type, @NotNull String name, @NotNull AST_NEW_EXP newExp) {
        super(type, name);
        this.newExp = newExp;
    }

    @NotNull
    @Override
    protected String name() {
        return "new var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(newExp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
        // no-op since just an header:
        // `semantHeader` takes care of  semanting`newExp` and checking if it is a valid assignment
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        //FIXME need to semant newExp, check if it is allowed by the rules, and set `representingType`
    }
}