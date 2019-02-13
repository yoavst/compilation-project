package ir.utils;

import ir.IRCommand;
import ir.arithmetic.*;
import ir.flow.IRGotoCommand;
import ir.flow.IRIfNotZeroCommand;
import ir.flow.IRIfZeroCommand;
import ir.flow.IRLabel;
import ir.functions.IRCallCommand;
import ir.functions.IRLabelMark;
import ir.functions.IRPopCommand;
import ir.functions.IRPushCommand;
import ir.memory.IRLoadCommand;
import ir.memory.IRLoadFromLabelCommand;
import ir.memory.IRStoreCommand;
import ir.registers.GlobalRegister;
import ir.registers.ParameterRegister;
import ir.registers.Register;
import symbols.Symbol;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeArray;
import utils.NotNull;
import utils.Nullable;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static utils.Utils.last;

public class IRContext {
    public static final int VIRTUAL_TABLE_OFFSET_IN_OBJECT = 4;
    public static final int ARRAY_DATA_INITIAL_OFFSET = 4;
    public static final int ARRAY_LENGTH_OFFSET = 0;
    public static final int PRIMITIVE_DATA_SIZE = 4; // pointer or int
    public static final int NIL_VALUE = 0;

    private static final int OBJECT_FIELDS_INITIAL_OFFSET = 8;
    private static final int VIRTUAL_TABLE_INITIAL_OFFSET = 0;
    private static final int ID_OFFSET_IN_OBJECT = 0;
    private static final int MAX_INT = 32767;
    private static final int MIN_INT = -32768;

    public static final IRLabel STDLIB_FUNCTION_MAIN = new IRLabel("main");
    private static final IRLabel STDLIB_FUNCTION_THROW_NULL = new IRLabel("___throwNull___");
    private static final IRLabel STDLIB_FUNCTION_THROW_DIVISION_BY_ZERO = new IRLabel("___throwDivisionByZero___");

    private static final IRLabel STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS = new IRLabel("___throwOutOfBounds___");
    private static final IRLabel STDLIB_FUNCTION_MALLOC = new IRLabel("___malloc___");
    private static final IRLabel STDLIB_FUNCTION_EXIT = new IRLabel("___exit___");

    public static final Symbol MAIN_SYMBOL = new Symbol("main", new TypeFunction("main"));
    public static final Register FIRST_FUNCTION_PARAMETER = new ParameterRegister(1);

    private final List<@NotNull LocalContext> localsStack = new ArrayList<>();
    private final List<@NotNull IRCommand> commands = new ArrayList<>();
    private final Map<@NotNull String, @NotNull IRLabel> strings = new HashMap<>();
    private final Map<@NotNull TypeClass, @NotNull ClassTable> classTables = new HashMap<>();
    private final List<@NotNull IRLabel> preMainFunctions = new ArrayList<>();

    //region Constant String labels
    private int labelCounter = 0;
    private final LabelGenerator constantStringLabelGenerator = s -> new IRLabel("__const_str_" + labelCounter++ + "__");

    /**
     * Return a label for a constant string to be loaded from
     */
    @NotNull
    public IRLabel labelForConstantString(@NotNull String s) {
        return strings.computeIfAbsent(s, constantStringLabelGenerator::newLabel);
    }
    //endregion

    //region Classes

    /**
     * Loads a class into the context by inflating its virtual table.
     */
    public void loadClass(@NotNull TypeClass clazz) {
        if (classTables.containsKey(clazz))
            return;
        if (clazz.parent != null)
            loadClass(clazz.parent);

        ClassTable table = new ClassTable(clazz, classTables.get(clazz.parent), VIRTUAL_TABLE_INITIAL_OFFSET, OBJECT_FIELDS_INITIAL_OFFSET);
        clazz.getMethods().forEach(table::insertFunction);
        clazz.getFields().forEach(table::insertField);
        classTables.put(clazz, table);
    }

    /**
     * Return the offset for the given field in the given class
     */
    public int getFieldOffset(@NotNull Symbol symbol) {
        return classTables.get(symbol.instance).getField(symbol);
    }

    /**
     * Return the offset for the given method in the virtual table fo the given class
     */
    public int getMethodOffsetInVtable(@NotNull Symbol symbol) {
        return classTables.get(symbol.instance).getFunction(symbol);
    }

    /**
     * Return memory allocation size needed to store class
     */
    public int sizeOf(@NotNull TypeClass clazz) {
        return classTables.get(clazz).getTotalSize();
    }
    //endregion

    //region Scope
    private LocalContext currentContext() {
        return last(localsStack);
    }

    public void addPreMainFunction(@NotNull IRLabel label) {
        preMainFunctions.add(label);
    }

    @NotNull
    public List<IRLabel> getPreMainFunctions() {
        return preMainFunctions;
    }

    public void openScope(@NotNull String name, @NotNull List<Symbol> newSymbols, ScopeType type, boolean isParameters, boolean isBounded) {
        RegisterAllocator allocator = localsStack.isEmpty() ? new SimpleRegisterAllocator() : last(localsStack).registerAllocator;
        LocalContext context = new LocalContext(name, allocator, new SimpleLabelGenerator(), type);

        // we assumes no inner function, so parameters are once per function
        int parameterCounter = 1;
        if (type == ScopeType.Function && isBounded) {
            parameterCounter++;
        }

        for (Symbol symbol : newSymbols) {
            if (symbol.isFunction()) {
                context.addFunction(symbol, generateFunctionLabelFor(symbol));
            } else if (isParameters) {
                context.addLocal(symbol, new ParameterRegister(parameterCounter++));
            } else if (type == ScopeType.Global) {
                context.addLocal(symbol, new GlobalRegister(parameterCounter++));
            } else {
                context.addLocal(symbol, allocator.newRegister());
            }
        }
        localsStack.add(context);
    }

    public void openObjectScope(@NotNull TypeClass clazz) {
        if (clazz.parent != null)
            openObjectScope(clazz.parent);

        List<Symbol> addedSymbols = clazz.getFields();
        addedSymbols.addAll(clazz.getMethods());
        openScope(clazz.name, addedSymbols, ScopeType.Class, false, true);
    }

    public void closeObjectScope() {
        while (currentContext().isClassScope())
            closeScope();
    }

    public void closeScope() {
        LocalContext last = currentContext();
        if (last.scopeType == ScopeType.Function)
            last.registerAllocator.freeAll();

        localsStack.remove(localsStack.size() - 1);
    }

    @NotNull
    public Register registerFor(@NotNull Symbol symbol) {
        for (int i = localsStack.size() - 1; i >= 0; i--) {
            LocalContext context = localsStack.get(i);
            if (context.hasLocal(symbol)) {
                return context.getRegister(symbol);
            }
        }
        throw new IllegalArgumentException("unknown symbol");
    }

    @NotNull
    public IRLabel functionLabelFor(@NotNull Symbol symbol) {
        for (int i = localsStack.size() - 1; i >= 0; i--) {
            LocalContext context = localsStack.get(i);
            if (context.hasFunction(symbol)) {
                return context.getFunction(symbol);
            }
        }
        throw new IllegalArgumentException("unknown symbol");
    }
    //endregion

    //region Commands
    @NotNull
    public Register newRegister() {
        return currentContext().registerAllocator.newRegister();
    }

    @NotNull
    public IRLabel newLabel(@Nullable String description) {
        return currentContext().labelGenerator.newLabel(description);
    }

    public void command(@NotNull IRCommand command) {
        commands.add(command);
    }


    public void label(@NotNull IRLabel label) {
        command(new IRLabelMark(label));
    }

    /**
     * Check if the value in the register is not null
     */
    public void checkNotNull(@NotNull Register register) {
        Register temp = newRegister();
        command(new IRBinOpRightConstCommand(temp, register, Operation.Equals, NIL_VALUE)); // temp = register == Nil
        IRLabel notNull = newLabel("notnull");
        command(new IRIfNotZeroCommand(temp, notNull)); // if temp jump notnull
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_NULL));
        label(notNull); // notnull:
    }

    /**
     * Check if the value in the register is not zero
     */
    public void checkNotZero(@NotNull Register register) {
        Register temp = newRegister();
        command(new IRBinOpRightConstCommand(temp, register, Operation.Equals, 0)); // temp = register == 0
        IRLabel notZero = newLabel("notZero");
        command(new IRIfNotZeroCommand(temp, notZero)); // if temp jump notnull
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_DIVISION_BY_ZERO));
        label(notZero); // notnull:
    }

    /**
     * Check if array access is within bounds
     */
    public void checkLength(@NotNull Register array, @NotNull Register index) {
        Register temp = newRegister();
        IRLabel outOfBounds = newLabel("outOfBounds");
        IRLabel inBounds = newLabel("inBounds");

        if (ARRAY_LENGTH_OFFSET != 0)
            command(new IRBinOpRightConstCommand(temp, array, Operation.Plus, ARRAY_LENGTH_OFFSET));
        else
            command(new IRSetValueCommand(temp, array));

        Register temp2 = newRegister();
        command(new IRLoadCommand(temp2, temp)); // temp2 = array.length
        Register temp3 = newRegister();
        command(new IRBinOpCommand(temp3, temp2, Operation.GreaterThan, index)); // temp3 = array.length > index
        command(new IRIfZeroCommand(temp, outOfBounds));
        command(new IRBinOpRightConstCommand(temp3, temp2, Operation.GreaterThan, -1)); // temp3 = array.length < index
        command(new IRIfZeroCommand(temp, outOfBounds));
        command(new IRGotoCommand(inBounds));
        label(outOfBounds);
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS));
        label(inBounds);
    }

    /**
     * keep result in [-2^15,2^15-1]
     */
    public Register binaryOpRestricted(Register left, Operation op, Register right) {
        Register result = newRegister();
        switch (op) {
            case Divide:
                checkNotZero(right);
            case Plus:
            case Times:
            case Minus:
                command(new IRBinOpCommand(result, left, op, right));

                // check if result in range
                Register temp = newRegister();
                IRLabel coerceDown = newLabel("coerce_down"), coerceUp = newLabel("coerce_up"), ok = newLabel("op_good");
                command(new IRBinOpRightConstCommand(temp, result, Operation.GreaterThan, MAX_INT));
                command(new IRIfNotZeroCommand(temp, coerceDown));
                command(new IRBinOpRightConstCommand(temp, result, Operation.LessThan, MIN_INT));
                command(new IRIfNotZeroCommand(temp, coerceUp));
                command(new IRGotoCommand(ok));
                label(coerceDown);
                command(new IRConstCommand(result, MAX_INT));
                command(new IRGotoCommand(ok));
                label(coerceUp);
                command(new IRConstCommand(result, MIN_INT));
                command(new IRGotoCommand(ok));
                label(ok);
                break;
            default:
                command(new IRBinOpCommand(result, left, op, right));
                break;
        }
        return result;
    }

    public Register malloc(@NotNull Register size) {
        command(new IRPushCommand(size));
        command(new IRCallCommand(STDLIB_FUNCTION_MALLOC));
        Register temp = newRegister();
        command(new IRPopCommand(temp));
        return temp;
    }

    public void exit() {
        command(new IRCallCommand(STDLIB_FUNCTION_EXIT));
    }

    public void assignVirtualTable(@NotNull Register thisReg, @NotNull TypeClass clazz) {
        Register offseted = newRegister();
        command(new IRBinOpRightConstCommand(offseted, thisReg, Operation.Plus, VIRTUAL_TABLE_OFFSET_IN_OBJECT));
        Register vtableAddress = newRegister();
        command(new IRLoadFromLabelCommand(vtableAddress, generateVirtualTableLabelFor(clazz)));
        command(new IRStoreCommand(offseted, vtableAddress));
        command(new IRBinOpRightConstCommand(offseted, thisReg, Operation.Plus, ID_OFFSET_IN_OBJECT));
        Register idRegister = newRegister();
        command(new IRConstCommand(vtableAddress, classTables.get(clazz).id));
        command(new IRStoreCommand(offseted, idRegister));
    }

    @NotNull
    public IRLabel constructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_ctor_" + clazz.name);
    }

    @NotNull
    public IRLabel internalConstructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_internal_ctor_" + clazz.name);
    }

    @NotNull
    public IRLabel constructorOf(@NotNull TypeArray array) {
        return new IRLabel("_ctor_array_" + array.arrayType.name);
    }

    @NotNull
    public IRLabel returnLabelFor(@NotNull Symbol symbol) {
        return new IRLabel("_return_" + symbol.toString());
    }

    @NotNull
    private IRLabel generateFunctionLabelFor(@NotNull Symbol symbol) {
        if (symbol.isBounded() && symbol.instance != null) {
            return new IRLabel("_f_" + symbol.instance.name + "_" + symbol.getName());
        } else
            return new IRLabel("_f_" + symbol.getName());
    }

    @NotNull
    public IRLabel generateVirtualTableLabelFor(@NotNull TypeClass clazz) {
        return new IRLabel("_vtable_" + clazz.name);
    }
    //endregion

    //region Local Context definition
    public enum ScopeType {
        Global,
        Class,
        Function,
        Inner
    }

    private class LocalContext {
        @NotNull
        private final String name;
        @NotNull
        private final RegisterAllocator registerAllocator;
        @NotNull
        private final LabelGenerator labelGenerator;
        @NotNull
        private final Map<@NotNull Symbol, @NotNull Register> locals = new HashMap<>();
        @NotNull
        private final Map<@NotNull Symbol, @NotNull IRLabel> functions = new HashMap<>();
        @NotNull
        private final ScopeType scopeType;

        LocalContext(@NotNull String name, @NotNull RegisterAllocator registerAllocator, @NotNull LabelGenerator labelGenerator, @NotNull ScopeType scopeType) {
            this.name = name;
            this.registerAllocator = registerAllocator;
            this.labelGenerator = labelGenerator;
            this.scopeType = scopeType;
        }

        LocalContext(@NotNull String name, ScopeType scopeType) {
            this(name, new SimpleRegisterAllocator(), new SimpleLabelGenerator(), scopeType);
        }

        void addLocal(@NotNull Symbol symbol, @NotNull Register register) {
            locals.put(symbol, register);
        }

        boolean hasLocal(@Nullable Symbol symbol) {
            return symbol != null && locals.containsKey(symbol);
        }

        void addFunction(@NotNull Symbol symbol, @NotNull IRLabel label) {
            functions.put(symbol, label);
        }

        boolean hasFunction(@Nullable Symbol symbol) {
            return symbol != null && functions.containsKey(symbol);
        }

        boolean isClassScope() {
            return scopeType == ScopeType.Class;
        }

        boolean isFunctionScope() {
            return scopeType == ScopeType.Function;
        }

        @NotNull
        Register getRegister(@NotNull Symbol symbol) {
            return locals.get(symbol);
        }

        @NotNull
        IRLabel getFunction(@NotNull Symbol symbol) {
            return functions.get(symbol);
        }

        @Override
        public String toString() {
            return name + "[" + Utils.toString(locals, Symbol::getName, Register::toString) + "]";
        }
    }
    //endregion

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        if (!strings.isEmpty()) {
            // strings
            for (String s : strings.keySet()) {
                IRLabel label = strings.get(s);
                result.append(label).append(" = \"").append(s).append("\";").append("\r\n");
            }
            result.append("\r\n").append("\r\n");
        }

        if (!classTables.isEmpty()) {
            // virtual tables
            for (TypeClass clazz : classTables.keySet()) {
                ClassTable classTable = classTables.get(clazz);
                result.append(generateVirtualTableLabelFor(clazz))
                        .append(" = [")
                        .append(
                                classTable.getFullVtable().entrySet()
                                        .stream()
                                        .sorted(Map.Entry.comparingByValue())
                                        .map(entry -> generateFunctionLabelFor(entry.getKey()).toString() + ":" + entry.getValue())
                                        .collect(Collectors.joining(", "))
                        ).append("];").append("\r\n");
            }
            result.append("\r\n").append("\r\n");
        }
        // globals
        assert localsStack.size() == 1;
        LocalContext context = currentContext();
        for (Symbol symbol : context.locals.keySet()) {
            if (symbol.isField()) {
                Register r = context.locals.get(symbol);
                result.append(r).append(" = &").append(symbol.getName()).append(";").append("\r\n");
            }
        }
        result.append("\r\n").append("\r\n");
        // commands
        for (IRCommand command : commands) {
            result.append(command).append("\r\n");
        }

        return result.toString();
    }

}
