package ast.declarations;

import ast.statements.AST_STMT;
import ir.arithmetic.IRSetValueCommand;
import ir.functions.IRReturnCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ThisRegister;
import ir.utils.IRContext;
import symbols.Symbol;
import symbols.SymbolTable;
import types.Type;
import types.TypeFunction;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Function;

public class AST_DEC_FUNC extends AST_DEC {
    @NotNull
    private final List<AST_ID> parameters;
    @NotNull
    private final AST_STMT[] statements;

    private TypeFunction representingType;
    private Symbol symbol;
    private List<Symbol> locals;

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
        symbolTable.beginScope(Function, null, symbol, "func" + name);
        for (AST_ID parameter : parameters) {
            symbolTable.enter(parameter.name, parameter.getType(), true, true);
        }
        for (AST_STMT statement : statements) {
            statement.semant(symbolTable);
        }
        Set<String> paramNames = parameters.stream().map(p -> p.name).collect(Collectors.toSet());
        locals = symbolTable.endScope().stream().filter(x -> !paramNames.contains(x.getName())).collect(Collectors.toList());
    }

    @Override
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        @SuppressWarnings("ConstantConditions")
        Type returnType = symbolTable.findGeneralizedType(type);
        if (returnType == null) {
            throwSemantic("Trying to declare a function with unknown return type: " + type);
        }

        representingType = new TypeFunction(name, returnType, new ArrayList<>(), symbolTable.getEnclosingClass());

        SemanticException deferredException = null;
        symbolTable.beginScope(Function, null, null, "funcParams " + name);
        try {
            for (AST_ID parameter : parameters) {
                parameter.semant(symbolTable);
            }
            parameters.stream().map(AST_ID::getType).forEachOrdered(representingType.params::add);
        } catch (SemanticException e) {
            deferredException = e;
        }
        symbolTable.endScope();

        // check scoping rules
        /*
         * section 3.7:
         *   global function/variable:
         *     different than defined types, functions, (assuming also variables)
         *   member function/variables:
         *     if parent had function/variable with same name - invalid.
         *     unless it is a function override
         *   cannot have the same name as class
         * table 10:
         *   shadowing a parent class member is invalid.
         *   shadowing a function parameter is invalid.
         *   shadowing a global variable with a class member is valid
         *   shadowing a variable from outside a function is valid.
         */
        if (symbolTable.getEnclosingClass() != null) {
            Symbol declaredFunc = symbolTable.getEnclosingClass().queryMethod(name);
            if (declaredFunc != null || symbolTable.findInCurrentScope(name) != null) {
                throwSemantic("Trying to declare the function \"" + name + "\", but the function is already declared (or field)");
            }
            declaredFunc = symbolTable.getEnclosingClass().queryMethodRecursively(name);
            if (declaredFunc != null && !declaredFunc.getFunction().sameSignature(representingType)) {
                throwSemantic("Trying to declare the function \"" + name + "\", but the function is already declared on parent class and it is not an override.");
            }

            if (symbolTable.getEnclosingClass().queryFieldRecursively(name) != null) {
                throwSemantic("Trying to declare the function \"" + name + "\", but a field with this name is already declared");
            }

            if (symbolTable.getEnclosingClass().name.equals(name)) {
                throwSemantic("Trying to declare the function \"" + name + "\", but this is the name of the class");
            }

        } else if (symbolTable.find(name) != null) {
            throwSemantic("Trying to declare the global function \"" + name + "\", but the name is already declared");
        }

        Type nameType = symbolTable.findGeneralizedType(name);
        if (nameType == TypeString.instance || nameType == TypeInt.instance || nameType == TypeVoid.instance) {
            throwSemantic("Trying to declare the function \"" + name + "\", but the name is of builtin type");
        }

        if (deferredException != null) {
            throw deferredException;
        }

        symbolTable.enter(name, representingType);
        symbol = symbolTable.findMethod(name, true);
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        // open scope
        context.openScope(symbol.toString(), parameters.stream().map(id -> new Symbol(id.name, id.getType())).collect(Collectors.toList()), IRContext.ScopeType.Function, true, symbol.isBounded());
        context.openScope(symbol.toString() + "_locals", locals, IRContext.ScopeType.Inner, false, false);

        context.label(context.functionLabelFor(symbol));

        if (symbol.isBounded()) {
            context.command(new IRSetValueCommand(ThisRegister.instance, IRContext.FIRST_FUNCTION_PARAMETER));
        }
        // function body
        for (AST_STMT statement : statements) {
            statement.irMe(context);
        }

        context.closeScope();
        context.closeScope();

        context.label(context.returnLabelFor(symbol));
        context.command(new IRReturnCommand());

        return NonExistsRegister.instance;
    }
}