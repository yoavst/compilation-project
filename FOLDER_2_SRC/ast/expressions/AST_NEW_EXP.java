package ast.expressions;

import ir.commands.functions.IRCallCommand;
import ir.commands.functions.IRPopCommand;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.SymbolTable;
import types.TypeClass;
import utils.NotNull;
import utils.errors.SemanticException;

public class AST_NEW_EXP extends AST_EXP {
    @NotNull
    String className;

    private TypeClass typeClass;

    public AST_NEW_EXP(@NotNull String className) {
        this.className = className;
    }

    @NotNull
    @Override
    protected String name() {
        return "new " + className;
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        typeClass = symbolTable.findClassType(className);
        if (typeClass == null) {
            throwSemantic("Trying to create a new expression of non class type: \"" + className + "\".");
        } else {
            type = typeClass;
        }
    }

    @NotNull
    @Override
    public Register irMe(IRContext context) {
        Register temp = context.newRegister();
        context.command(new IRCallCommand(context.constructorOf(typeClass)));
        context.command(new IRPopCommand(temp));
        return temp;
    }
}
