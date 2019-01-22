package ast.expressions;

import types.builtins.TypeString;
import utils.NotNull;

public class AST_EXP_STRING extends AST_EXP_CONSTANT {
    @NotNull
    public String value;

    public AST_EXP_STRING(@NotNull String value) {
        super(TypeString.instance);
        this.value = value;
    }

    @NotNull
    @Override
    protected String name() {
        if (value.length() > 5)
            return "STR(" + value.substring(0, 5) + "...)";
        return "STR(" + value + ")";
    }
}
