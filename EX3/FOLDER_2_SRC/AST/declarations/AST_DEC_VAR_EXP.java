package ast.declarations;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.Type;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    @Nullable
    public AST_EXP exp;

    public Type representingType;

    public AST_DEC_VAR_EXP(@NotNull String type,@NotNull String name) {
        super(type, name);
    }

    public AST_DEC_VAR_EXP(@NotNull String type, @NotNull String name, @Nullable AST_EXP exp) {
        super(type, name);
        this.exp = exp;
    }

    @NotNull
    @Override
    protected String name() {
        return "var " + type + " " + name;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(exp);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
        // no-op since just an header:
        // `semantHeader` takes care of  semanting `exp` and checking if it is a valid assignment
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        //FIXME need to semant exp, check if it is allowed by the rules, and set `representingType`
    }
}