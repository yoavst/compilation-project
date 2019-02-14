package ast.declarations;

import ast.expressions.AST_EXP;
import ast.expressions.AST_EXP_CONSTANT;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRSetValueCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.flow.IRLabel;
import ir.commands.functions.IRReturnCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ThisRegister;
import ir.utils.IRContext;
import symbols.Symbol;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.errors.SemanticException;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    @Nullable
    public AST_EXP exp;

    private Type representingType;
    private Symbol symbol;
    private Symbol enclosingFunction;

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
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        //noinspection ConstantConditions
        representingType = symbolTable.findGeneralizedType(type);
        if (representingType == null || representingType == TypeVoid.instance) {
            throwSemantic("Trying to declare a variable of unknown type: " + type);
        }

        if (exp != null) {
            exp.semant(symbolTable);
            if (!representingType.isAssignableFrom(exp.getType())) {
                throwSemantic("Trying to declare a variable of type " + type + " but received " + exp.getType());
            } else if (symbolTable.getEnclosingFunction() == null && symbolTable.getEnclosingClass() != null) {
                /* Section 3.2 in manual
                 * class member can only be initialized by a simple constant expression: int | nil | string
                 */
                if (!(exp instanceof AST_EXP_CONSTANT)) {
                    throwSemantic("Trying to declare a class member \"" + name + "\" but using non-constant initializer");
                }
            }
        }

        // check scoping rules
        if (!canBeDefined(symbolTable, name)) {
            throwSemantic("Trying to define the variable \"" + name +"\", but it violates the scoping rules");
        }

        symbolTable.enter(name, representingType, true);

        symbol = symbolTable.find(name);
        enclosingFunction = symbolTable.getEnclosingFunction();
    }

    @Override
    public Type getType() {
        return representingType;
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        if (symbol.isBounded()) {
            // will be called when writing to the constructor_init
            // initiate and save to memory
            if (exp != null) {
                Register content = exp.irMe(context);

                int fieldOffset = context.getFieldOffset(symbol);
                Register thisReg = ThisRegister.instance;
                Register temp = context.newRegister();
                context.command(new IRBinOpRightConstCommand(temp, thisReg, Operation.Plus, fieldOffset)); // temp hold address of variable
                context.command(new IRStoreCommand(temp, content));
            }
        } else if (enclosingFunction != null) {
            // will be called when going over function.
            // initiate and save to register
            if (exp != null) {
                Register variable = context.registerFor(symbol);
                Register content = exp.irMe(context);

                context.command(new IRSetValueCommand(variable, content));
            }
        } else if (exp != null){
            // need to add pre-main hook.
            IRLabel label = context.newLabel("hook_init_" + symbol.getName());
            context.addPreMainFunction(label);

            context.label(label);
            Register variable = context.registerFor(symbol);
            Register content = exp.irMe(context);
            context.command(new IRSetValueCommand(variable, content));
            context.command(new IRReturnCommand());
        }
        return NonExistsRegister.instance;
    }
}