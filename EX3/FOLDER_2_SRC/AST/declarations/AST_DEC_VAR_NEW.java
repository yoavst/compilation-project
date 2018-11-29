package ast.declarations;

import ast.expressions.AST_NEW_EXP;
import utils.NotNull;
import utils.Nullable;

public class AST_DEC_VAR_NEW extends AST_DEC_VAR {
    @NotNull
    public AST_NEW_EXP newExp;

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
}