package ast.expressions;

import ir.utils.IRContext;
import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.Operation;
import ir.registers.Register;
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


    public AST_EXP_BINOP(@NotNull AST_EXP left, @NotNull AST_EXP right, @NotNull Op op) {
        this.left = left;
        this.right = right;
        this.op = op;
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
        Plus("+"), Minus("-"), Times("*"), Divide("/"), LT("<"), GT(">"), EQ("=");
        @NotNull
        String text;

        Op(@NotNull String text) {
            this.text = text;
        }
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        left.semant(symbolTable);
        right.semant(symbolTable);

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

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        Register leftRegister = left.irMe(context);
        Register rightRegister = right.irMe(context);
        if (TypeString.instance.equals(left.getType()) && TypeString.instance.equals(right.getType())) {
            Register temp = context.newRegister();
            if (op == Op.Plus)
                context.command(new IRBinOpCommand(temp, leftRegister, Operation.Concat, rightRegister));
            else
                context.command(new IRBinOpCommand(temp, leftRegister, Operation.StrEquals, rightRegister));
            return temp;
        } else {
            return context.binaryOpRestricted(leftRegister, Operation.fromAstOp(op), rightRegister);
        }
    }
}
