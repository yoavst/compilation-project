package ast.declarations;

import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRSetValueCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.flow.IRLabel;
import ir.commands.functions.IRReturnCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ReturnRegister;
import ir.utils.IRContext;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeArray;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.Collections;

public class AST_DEC_ARRAY extends AST_DEC {
    TypeArray array;

    public AST_DEC_ARRAY(@NotNull String type, @NotNull String name) {
        super(type, name);
    }

    @NotNull
    @Override
    protected String name() {
        return "Arr: " + type + "[] " + name;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) {
       // no-op since just an header
    }

    @Override
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        @SuppressWarnings("ConstantConditions")
        Type arrayType = symbolTable.findGeneralizedType(type);
        if (arrayType == null) {
            throwSemantic("Trying to declare an array type of an unknown type: \"" + type + "\"");
        } else if (arrayType == TypeVoid.instance) {
            throwSemantic("Trying to declare an array type of type void.");
        }

        // check scoping rules
        if (symbolTable.find(name) != null) {
            throwSemantic("Trying to declare an array typedef but the name \"" + name + "\" is already in use");
        }
        array = new TypeArray(name, arrayType);
        symbolTable.enter(name, array, false, true);
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        // create constructor
        IRLabel constructorLabel = context.constructorOf(array);

        context.openScope(constructorLabel.toString(), Collections.emptyList(), IRContext.ScopeType.Function, false, false);
        // getting size as first parameter
        context.label(constructorLabel);
        // calculate the right size to allocate
        Register allocationSize = context.newRegister();
        context.command(new IRBinOpRightConstCommand(allocationSize, IRContext.FIRST_FUNCTION_PARAMETER, Operation.Plus, IRContext.ARRAY_DATA_INITIAL_OFFSET));
        // call malloc
        Register mallocResult = context.malloc(allocationSize);
        // save length
        Register temp = context.newRegister();
        context.command(new IRBinOpRightConstCommand(temp, mallocResult, Operation.Plus, IRContext.ARRAY_LENGTH_OFFSET));
        context.command(new IRStoreCommand(temp, IRContext.FIRST_FUNCTION_PARAMETER));
        // return
        context.command(new IRSetValueCommand(ReturnRegister.instance, mallocResult));
        context.command(new IRReturnCommand());
        context.closeScope();

        return NonExistsRegister.instance;
    }
}