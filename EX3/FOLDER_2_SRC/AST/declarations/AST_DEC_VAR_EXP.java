package ast.declarations;

import ast.expressions.AST_EXP;
import symbols.SymbolTable;
import types.Type;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.SemanticException;

public class AST_DEC_VAR_EXP extends AST_DEC_VAR {
    @Nullable
    public AST_EXP exp;

    public Type representingType;

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
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        //noinspection ConstantConditions
        representingType = symbolTable.findGeneralizedType(type);
        if (representingType == null || representingType == TypeVoid.instance) {
            throwSemantic("Trying to declare a variable of unknown type: " + type);
        }

        if (exp != null) {
            exp.semant(symbolTable);
            //TODO check if allowed to declare a variable with this exp (i.e class fields need to have constant initializer)
            if (!representingType.equals(exp.getType())) {
                throwSemantic("Trying to declare a variable of type " + type + " but received " + exp.getType());
            }
        }

        // TODO check if not in scope yet
        symbolTable.enter(name, representingType, true);
    }
}