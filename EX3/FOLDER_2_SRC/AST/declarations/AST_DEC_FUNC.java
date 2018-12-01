package ast.declarations;

import ast.statements.AST_STMT;
import symbols.SymbolTable;
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
        symbolTable.beginScope(Function, representingType);
        for (AST_ID parameter : parameters) {
            symbolTable.enter(parameter.name, parameter.getType(), true);
        }
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
        representingType = new TypeFunction(name, returnType, parameterTypes);


        // check scoping rules
        /*
         * section 3.7:
         *   global function/variable:
         *     different than defined types, functions, (assuming also variables)
         *   member function/variables:
         *     if parent had function/variable with same name - invalid.
         *     unless it is a function override
         * table 10:
         *   shadowing a parent class member is invalid.
         *   shadowing a function parameter is invalid.
         *   shadowing a global variable with a class member is valid
         *   shadowing a variable from outside a function is valid.
         */
        if (symbolTable.getEnclosingClass() != null) {
            TypeFunction declaredFunc = symbolTable.getEnclosingClass().queryMethod(name);
            if (declaredFunc != null) {
                throwSemantic("Trying to declare the function \"" + name +"\", but the function is already declared");
            }
            declaredFunc = symbolTable.getEnclosingClass().queryMethodRecursively(name);
            if (declaredFunc != null && !declaredFunc.sameSignature(representingType)) {
                throwSemantic("Trying to declare the function \"" + name +"\", but the function is already declared on parent class and it is not an override.");
            }

            if (symbolTable.getEnclosingClass().queryFieldRecursively(name) != null) {
                throwSemantic("Trying to declare the function \"" + name +"\", but a field with this name is already declared");
            }

        } else if (symbolTable.find(name) != null) {
                throwSemantic("Trying to declare the global function \"" + name +"\", but the name is already declared");
        }

        symbolTable.enter(name, representingType);
    }
}