package ast.declarations;

import ir.commands.arithmetic.IRConstCommand;
import ir.commands.arithmetic.IRSetValueCommand;
import ir.commands.flow.IRLabel;
import ir.commands.functions.*;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.registers.ReturnRegister;
import ir.registers.ThisRegister;
import ir.utils.IRContext;
import symbols.SymbolTable;
import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.ClassScan;
import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Class;
import types.TypeClass;
import utils.NotNull;
import utils.Nullable;
import utils.errors.SemanticException;

import java.util.Collections;

public class AST_DEC_CLASS extends AST_DEC {
    @Nullable
    private String parentClass;
    @NotNull
    private AST_DEC[] fields;

    private TypeClass representingType;

    public AST_DEC_CLASS(@NotNull String name, @NotNull AST_DEC[] fields, @Nullable String parentClass) {
        super(null, name);

        this.fields = fields;
        this.parentClass = parentClass;
    }

    public AST_DEC_CLASS(@NotNull String name, @NotNull AST_DEC[] fields) {
        this(name, fields, null);
    }


    @NotNull
    @Override
    protected String name() {
        return "Class " + name + (parentClass == null ? "" : (" extends " + parentClass));
    }

    @Override
    public void printMe() {
        super.printMe();
        addListUnderWrapper("body", fields);
    }

    @Override
    public void semantMe(SymbolTable symbolTable) throws SemanticException {
        symbolTable.beginScope(ClassScan, representingType, null,"classScan " + name);
        SemanticException headerException = null;
        AST_DEC exceptionField = null;
        for (AST_DEC field : fields) {
            try {
                field.semantHeader(symbolTable);
            } catch (SemanticException e) {
                if (headerException == null) {
                    headerException = e;
                    exceptionField = field;
                }
            }
        }
        symbolTable.endScope();
        // Since it is a class scan scope, the symbol table will merge all the inner declarations into the class type

        // now, we need to semant every function. However, if we add an exception earlier,
        // we'll have to report it on the right time, because we need to report the first error.
        symbolTable.beginScope(Class, representingType, null, "class " + name);
        for (AST_DEC field : fields) {
            if (headerException != null && exceptionField == field) {
                throw headerException;
            }
            field.semant(symbolTable);
        }
        symbolTable.endScope();
    }

    @Override
    public void semantMeHeader(SymbolTable symbolTable) throws SemanticException {
        TypeClass parent = null;
        if (parentClass != null) {
            parent = symbolTable.findClassType(parentClass);
            if (parent == null) {
                throwSemantic("Trying to declare a class extending unknown class type: <" + parentClass + ">");
            }
        }
        representingType = new TypeClass(name, parent);

        // check scoping rules
        if (symbolTable.find(name) != null) {
            throwSemantic("Trying to declare a class but the name \"" + name + "\" is already in use");
        }

        symbolTable.enter(name, representingType, false,true, true);
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        context.loadClass(representingType);
        int size = context.sizeOf(representingType);

        IRLabel constructorLabel = context.constructorOf(representingType);
        context.openScope(constructorLabel.toString(), Collections.emptyList(), IRContext.ScopeType.Function, false, false);

        context.label(constructorLabel);
        context.command(new IRFunctionInfo(constructorLabel.toString(),0, 0));
        Register allocationSize = context.newRegister();
        context.command(new IRConstCommand(allocationSize, size));
        Register thisReg = context.malloc(allocationSize);
        context.assignVirtualTable(thisReg, representingType);

        Register temp = context.newRegister();

        callInternalConstructor(representingType, context, thisReg, temp);


        // return instance
        context.command(new IRSetValueCommand(ReturnRegister.instance, thisReg));
        context.label(context.returnLabelForConstructor(representingType));
        context.command(new IRReturnCommand());

        context.closeScope();

        context.openObjectScope(representingType);

        // now, internal constructor
        IRLabel internalLabel = context.internalConstructorOf(representingType);
        context.label(internalLabel);
        context.command(new IRFunctionInfo(internalLabel.toString(), 1, 0));
        context.openScope(internalLabel.toString(), Collections.emptyList(), IRContext.ScopeType.Function, false, false);

        for (AST_DEC field : fields) {
            if (field instanceof AST_DEC_VAR) {
                field.irMe(context);
            }
        }
        context.label(context.returnLabelForInternalConstructor(representingType));
        context.command(new IRReturnCommand());
        context.closeScope();

        // now, class body
        for (AST_DEC field : fields) {
            if (!(field instanceof AST_DEC_VAR)) {
                field.irMe(context);
            }
        }

        context.closeObjectScope();

        return NonExistsRegister.instance;
    }

    private void callInternalConstructor(TypeClass current, IRContext context, Register thisRegister, Register temp) {
        if (current.parent != null) {
            callInternalConstructor(current.parent, context, thisRegister, temp);
        }

        context.command(new IRPushCommand(thisRegister));
        context.command(new IRCallCommand(context.internalConstructorOf(current)));
        context.command(new IRPopCommand(temp));
    }
}