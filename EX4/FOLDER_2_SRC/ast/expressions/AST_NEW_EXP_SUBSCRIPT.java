package ast.expressions;

import ir.utils.IRContext;
import ir.commands.functions.IRCallCommand;
import ir.commands.functions.IRPopCommand;
import ir.commands.functions.IRPushCommand;
import ir.registers.Register;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeArray;
import types.builtins.TypeArrayUnspecified;
import types.builtins.TypeInt;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.errors.SemanticException;


public class AST_NEW_EXP_SUBSCRIPT extends AST_NEW_EXP {
    @NotNull
    public AST_EXP subscript;

    public AST_NEW_EXP_SUBSCRIPT(String className, @NotNull AST_EXP subscript) {
        super(className);
        this.subscript = subscript;
    }

    @NotNull
    @Override
    protected String name() {
        return "new " + className + "[...]";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(subscript);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        subscript.semant(symbolTable);

        Type classType = symbolTable.findGeneralizedType(className);
        if (classType == TypeVoid.instance) {
            throwSemantic("Trying to create an array of void");
        } else if (classType != null) {
            // valid expression: new TypeArray[int expression]
            if (subscript.getType() == TypeInt.instance) {
                type = classType;
            } else {
                throwSemantic("Trying to create an array of \"" + className + "\" not of integral size: " + subscript);
            }
        } else {
            // Since poseidon classes do not have a constructor, it's an error.
            throwSemantic("Trying to create a new[] expression of invalid type: \"" + className + "\".");
        }
        type = new TypeArrayUnspecified(classType);
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        assert type != null;

        // push size
        Register arraySize = subscript.irMe(context);
        context.command(new IRPushCommand(arraySize));
        // cal constructor
        context.command(new IRCallCommand(context.constructorOf(((TypeArray) type))));
        // pop array
        Register temp = context.newRegister();
        context.command(new IRPopCommand(temp));
        return temp;
    }
}
