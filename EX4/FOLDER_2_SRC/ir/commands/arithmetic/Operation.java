package ir.commands.arithmetic;

import ast.expressions.AST_EXP_BINOP;
import ir.utils.IRContext;
import utils.NotNull;

public enum Operation {
    Plus("+"), Minus("-"), Times("*"), Divide("/"), Equals("=="), GreaterThan(">"), LessThan("<"), Concat("concat"), StrEquals("~"),
    BoundedPlus("⊕"),  BoundedMinus("⊖"),  BoundedDivide("⊗"),  BoundedTimes("⊘");

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

    public static Operation fromAstOpBounded(AST_EXP_BINOP.Op op) {
        switch (op) {
            case Plus:
                return BoundedPlus;
            case Minus:
                return BoundedMinus;
            case Times:
                return BoundedTimes;
            case Divide:
                return BoundedDivide;
            case LT:
                return LessThan;
            case GT:
                return GreaterThan;
            case EQ:
                return Equals;
        }
        return null;
    }

    public static int evaluate(int l, Operation op, int r) {
        switch (op) {
            case Plus:
                return l + r;
            case Minus:
                return l - r;
            case Times:
                return l * r;
            case Divide:
                return l / r;
            case LessThan:
                return l < r ? 1 : 0;
            case GreaterThan:
                return l > r ? 1 : 0;
            case Equals:
                return l == r ? 1 : 0;
            case BoundedPlus:
                return coerce(l + r);
            case BoundedMinus:
                return coerce(l - r);
            case BoundedTimes:
                return coerce(l * r);
            case BoundedDivide:
                return coerce(l / r);
        }
        return -1;
    }

    private static int coerce(int value) {
        if (value > IRContext.MAX_INT)
            return IRContext.MAX_INT;
        else if (value < IRContext.MIN_INT) {
            return IRContext.MIN_INT;
        } else {
            return value;
        }
    }
}

