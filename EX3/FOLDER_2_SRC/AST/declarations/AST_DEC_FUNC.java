package ast.declarations;

import ast.statements.AST_STMT;
import symbols.SymbolTable;
import types.TYPE_FOR_SCOPE_BOUNDARIES;
import types.Type;
import types.TypeFunction;
import utils.NotNull;
import utils.SemanticException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Function;

public class AST_DEC_FUNC extends AST_DEC {
    @NotNull
    public List<AST_ID> parameters;
    @NotNull
    public AST_STMT[] statements;

    public TypeFunction representingType;

    public AST_DEC_FUNC(@NotNull String type, @NotNull String name, @NotNull AST_STMT[] statements, @NotNull List<AST_ID> parameters) {
        super(type, name);

        this.parameters = parameters;
        this.statements = statements;
    }

    public AST_DEC_FUNC(@NotNull String type, @NotNull String name, @NotNull AST_STMT[] statements) {
        this(type, name, statements, Collections.emptyList());
    }

    @NotNull
    @Override
    protected String name() {
        return "fun " + name + "(...): " + type;
    }

    @Override
    public void printMe() {
        super.printMe();
        addListUnderWrapper("parameters", parameters);
        addListUnderWrapper("body", statements);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        symbolTable.beginScope(Function);
        for (AST_STMT statement : statements) {
            statement.semant(symbolTable);
        }
        symbolTable.endScope();
    }

    @Override
    public void semantHeader(SymbolTable symbolTable) throws SemanticException {
        @SuppressWarnings("ConstantConditions")
        Type returnType = symbolTable.findGeneralizedType(type);
        if (returnType == null) {
            throwSemantic("Trying to declare a function with unknown return type: " + type);
        }

        for (AST_ID parameter : parameters) {
            parameter.semant(symbolTable);
        }
        List<Type> parameterTypes = parameters.stream().map(AST_ID::getType).collect(Collectors.toList());


        // TODO check if not in scope yet
        representingType = new TypeFunction(name, returnType, parameterTypes);
        symbolTable.enter(name, representingType);
    }
}