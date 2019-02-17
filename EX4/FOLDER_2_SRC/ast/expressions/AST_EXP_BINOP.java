package ast.expressions;

import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRConstCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.memory.IRLoadAddressFromLabelCommand;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import types.TypeError;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_EXP_BINOP extends AST_EXP {
    @NotNull
    public Op op;
    @NotNull
    private AST_EXP left;
    @NotNull
    private AST_EXP right;
    private boolean isConst = false;


    public AST_EXP_BINOP(@NotNull AST_EXP left, @NotNull AST_EXP right, @NotNull Op op) {
        this.left = left;
        this.right = right;
        this.op = op;
    }

    @Override
    boolean isConst() {
        return isConst;
    }

    @NotNull
    @Override
    protected String name() {
        return op.text;
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(left);
        printAndEdge(right);
    }

    public enum Op {
        Plus("+", true), Minus("-", false),
        Times("*", true), Divide("/", false), LT("<", false),
        GT(">", false), EQ("=", true);
        @NotNull
        String text;
        boolean isSymmetric;

        Op(@NotNull String text, boolean isSymmetric) {
            this.text = text;
            this.isSymmetric = isSymmetric;
        }
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        left.semant(symbolTable);
        right.semant(symbolTable);

        if (left.isConst() && right.isConst())
            isConst = true;

        if (op == Op.EQ) {
            /* Section 3.5 in manual
             * NIL <=> array, class
             * array <=> same type of array
             * class <=> assignable class
             * int <=> int
             * str <=> str
             *
             * returns int
             */
            if (!left.getType().isAssignableFrom(right.getType()) && !right.getType().isAssignableFrom(left.getType())) {
                throwSemantic("Trying to compare an object of type " + left.getType() + " to object of type: " + right.getType());
            }
            type = TypeInt.instance;
        } else if (op != Op.Plus) {
            /* Section 3.6 in manual
             * int OP int => int
             */
            if (left.type != TypeInt.instance && left.type != TypeError.instance) {
                throwSemantic("Trying to apply binary operation " + op.text + " but left expression is not int: " + left);
            } else if (right.type != TypeInt.instance && right.type != TypeError.instance) {
                throwSemantic("Trying to apply binary operation " + op.text + " but right expression is not int: " + right);
            } else {
                type = TypeInt.instance;
            }
        } else {
            /* Section 3.6 in manual
             * int + int => int
             * str + str => str
             */
            if (left.type == TypeError.instance && right.type == TypeError.instance) {
                type = TypeError.instance;
            } else if ((left.type == TypeInt.instance || left.type == TypeError.instance) && (right.type == TypeInt.instance || right.type == TypeError.instance)) {
                type = TypeInt.instance;
            } else if ((left.type == TypeString.instance || left.type == TypeError.instance) && (right.type == TypeString.instance || right.type == TypeError.instance)) {
                type = TypeString.instance;
            } else {
                throwSemantic("Trying to apply binary operation + but typing is incorrect: left is " + left.type + " and right is " + right.type);
            }
        }
    }

    @Override
    Object getConstValue() {
        assert left.isConst() && right.isConst();
        if (left.getType() == TypeString.instance) {
            String leftString = (String) left.getConstValue(), rightString = (String) right.getConstValue();
            if (op == Op.Plus) {
                return leftString + rightString;
            } else {
                // equality
                return leftString.equals(rightString) ? 1 : 0;
            }
        } else {
            // integers
            int l = (int) left.getConstValue(), r = (int) right.getConstValue();
            switch (op) {
                case Plus:
                    return coerce(l + r);
                case Minus:
                    return coerce(l - r);
                case Times:
                    return coerce(l * r);
                case Divide:
                    return coerce(l / r);
                case LT:
                    return l < r ? 1 : 0;
                case GT:
                    return l > r ? 1 : 0;
                case EQ:
                    return l == r ? 1 : 0;
            }
            return -1; // cannot reach here
        }
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        // constant expression, can evaluate it
        if (left.isConst() && right.isConst()) {
            Register temp = context.newRegister();
            if (left.getType() == TypeString.instance) {
                if (op == Op.Plus) {
                    context.command(new IRLoadAddressFromLabelCommand(temp, context.labelForConstantString((String) getConstValue())));
                    return temp;
                } else {
                    // equality
                    context.command(new IRConstCommand(temp, (Integer) getConstValue()));
                }
            } else {
                context.command(new IRConstCommand(temp, (Integer) getConstValue()));
            }
            return temp;
        }

        if (left.isConst() && op.isSymmetric) {
                // switch between them
                AST_EXP temp = left;
                left = right;
                right = temp;
        }

        if (right.isConst() && !TypeString.instance.equals(left.getType())) {
            // only support inline integral operations
            Register leftRegister = left.irMe(context);
            Register temp = context.newRegister();
            context.command(new IRBinOpRightConstCommand(temp, leftRegister, Operation.fromAstOpBounded(op), ((Integer) right.getConstValue())));
            return temp;
        }


        Register leftRegister = left.irMe(context);
        Register rightRegister = right.irMe(context);
        Register temp = context.newRegister();

        if (TypeString.instance.equals(left.getType()) && TypeString.instance.equals(right.getType())) {
            if (op == Op.Plus) {
                context.checkNotNull(leftRegister);
                context.checkNotNull(rightRegister);
                context.command(new IRBinOpCommand(temp, leftRegister, Operation.Concat, rightRegister));
            } else {
                context.command(new IRBinOpCommand(temp, leftRegister, Operation.StrEquals, rightRegister));
            }
        } else {
            context.command(new IRBinOpCommand(temp, leftRegister, Operation.fromAstOpBounded(op), rightRegister));
        }
        return temp;
    }

    private int coerce(int value) {
        if (value > IRContext.MAX_INT)
            return IRContext.MAX_INT;
        else if (value < IRContext.MIN_INT) {
            return IRContext.MIN_INT;
        } else {
            return value;
        }
    }
}
