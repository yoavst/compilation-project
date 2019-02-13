package ast.declarations;

import ast.expressions.AST_NEW_EXP;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.IRSetValueCommand;
import ir.arithmetic.Operation;
import ir.flow.IRLabel;
import ir.functions.IRReturnCommand;
import ir.memory.IRStoreCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ThisRegister;
import ir.utils.IRContext;
import symbols.Symbol;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_DEC_VAR_NEW extends AST_DEC_VAR {
    @NotNull
    public AST_NEW_EXP newExp;

    private Type representingType;
    private Symbol symbol;
    private Symbol enclosingFunction;

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
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        //noinspection ConstantConditions
        representingType = symbolTable.findGeneralizedType(type);
        if (representingType == null || representingType == TypeVoid.instance) {
            throwSemantic("Trying to declare a variable of unknown type: " + type);
        }

        newExp.semant(symbolTable);
        if (!representingType.isAssignableFrom(newExp.getType())) {
            throwSemantic("Trying to declare a variable of type " + type + " but received new " + newExp.getType());
        } else if (symbolTable.getEnclosingFunction() == null && symbolTable.getEnclosingClass() != null) {
            /* Section 3.2 in manual
             * class member can only be initialized by a simple constant expression: int | nil | string
             * Specifically, new initializer is not a constant.
             */
            throwSemantic("Trying to declare a class member \"" + name + "\" but using non-constant new initializer");
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
            Register content = newExp.irMe(context);

            int fieldOffset = context.getFieldOffset(symbol);
            Register thisReg = ThisRegister.instance;
            Register temp = context.newRegister();
            context.command(new IRBinOpRightConstCommand(temp, thisReg, Operation.Plus, fieldOffset)); // temp hold address of variable
            context.command(new IRStoreCommand(temp, content));
        } else if (enclosingFunction != null) {
            // will be called when going over function.
            // initiate and save to register
            Register variable = context.registerFor(symbol);
            Register content = newExp.irMe(context);

            context.command(new IRSetValueCommand(variable, content));
        } else {
            // need to add pre-main hook.
            IRLabel label = context.newLabel("hook_init_" + symbol.getName());
            context.addPreMainFunction(label);

            context.label(label);
            Register variable = context.registerFor(symbol);
            Register content = newExp.irMe(context);
            context.command(new IRSetValueCommand(variable, content));
            context.command(new IRReturnCommand());
        }
        return NonExistsRegister.instance;
    }
}