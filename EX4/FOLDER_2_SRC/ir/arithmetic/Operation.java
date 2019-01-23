package ir.arithmetic;

import ast.expressions.AST_EXP_BINOP;
import utils.NotNull;

public enum Operation {
    Plus("+"), Minus("-"), Times("*"), Divide("/"), Equals("=="), GreaterThan(">"), LessThan("<"), Or("||"), And("&&");

    @NotNull
    String text;

    Operation(@NotNull String text) {
        this.text = text;
    }

    public static Operation fromAstOp(AST_EXP_BINOP.Op op) {
        switch (op) {
            case Plus:
                return Plus;
            case Minus:
                return Minus;
            case Times:
                return Times;
            case Divide:
                return Divide;
            case LT:
                return LessThan;
            case GT:
                return GreaterThan;
            case EQ:
                return Equals;
        }
        return null;
    }
}

