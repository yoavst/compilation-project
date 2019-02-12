package ir.utils;

import ir.IRCommand;
import ir.arithmetic.IRBinOpCommand;
import ir.arithmetic.IRBinOpRightConstCommand;
import ir.arithmetic.Operation;
import ir.flow.IRGotoCommand;
import ir.flow.IRIfNotZeroCommand;
import ir.flow.IRIfZeroCommand;
import ir.flow.IRLabel;
import ir.functions.IRCallCommand;
import ir.functions.IRLabelMark;
import ir.memory.IRLoadCommand;
import ir.memory.IRStoreCommand;
import ir.registers.ParameterRegister;
import ir.registers.Register;
import symbols.Symbol;
import types.TypeClass;
import types.builtins.TypeArray;
import utils.NotNull;
import utils.Nullable;
import utils.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import static utils.Utils.last;

public class IRContext {
    public static final int VIRTUAL_TABLE_OFFSET_IN_OBJECT = 4;
    public static final int ARRAY_DATA_INITIAL_OFFSET = 4;
    public static final int NIL_VALUE = Integer.MAX_VALUE;
    private static final int VIRTUAL_TABLE_INITIAL_OFFSET = 0;
    private static final int OBJECT_FIELDS_INITIAL_OFFSET = 8;
    private static final int ARRAY_LENGTH_OFFSET = 0;

    private static final IRLabel STDLIB_FUNCTION_THROW_NULL = new IRLabel("throwNull");
    private static final IRLabel STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS = new IRLabel("throwOutOfBounds");

    @NotNull
    private final List<@NotNull LocalContext> localsStack = new ArrayList<>();
    @NotNull
    private final List<@NotNull IRCommand> commands = new ArrayList<>();
    @NotNull
    private final Map<@NotNull String, @NotNull IRLabel> strings = new HashMap<>();
    @NotNull
    private final Map<@NotNull TypeClass, @NotNull ClassTable> classTables = new HashMap<>();

    //region Constant String labels
    private int labelCounter = 0;
    @NotNull
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
    //endregion

    //region Scope
    private LocalContext currentContext() {
        return last(localsStack);
    }

    public void openScope(@NotNull String name, @NotNull List<Symbol> newSymbols, @NotNull Predicate<Symbol> isParameter, boolean isClassScope) {
        RegisterAllocator allocator = localsStack.isEmpty() ? new SimpleRegisterAllocator() : last(localsStack).registerAllocator;
        LocalContext context = new LocalContext(name, allocator, new SimpleLabelGenerator());
        if (isClassScope)
            context.asClassScope();

        // we assumes no inner function, so parameters are once per function
        int parameterCounter = 1;
        for (Symbol symbol : newSymbols) {
            if (symbol.isFunction()) {
                context.addFunction(symbol, generateFunctionLabelFor(symbol));
            } else if (isParameter.test(symbol)) {
                context.addLocal(symbol, new ParameterRegister(parameterCounter++));
            } else {
                context.addLocal(symbol, allocator.newRegister());
            }
        }
        localsStack.add(context);
    }

    public void openScope(@NotNull String name, @NotNull List<Symbol> newSymbols) {
        openScope(name, newSymbols, s -> false, false);
    }

    public void openObjectScope(@NotNull TypeClass clazz) {
        if (clazz.parent != null)
            openObjectScope(clazz.parent);

        List<Symbol> addedSymbols = clazz.getFields();
        addedSymbols.addAll(clazz.getMethods());
        openScope(clazz.name, addedSymbols, s -> false, true);
    }

    public void closeObjectScope() {
        while (currentContext().isClassScope())
            closeScope();
    }

    public void closeScope() {
        if (localsStack.size() == 2) {
            localsStack.get(1).registerAllocator.freeAll();
        }
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

    public void checkLength(@NotNull Register array, @NotNull Register index) {
        Register temp = newRegister();
        IRLabel outOfBounds = newLabel("outOfBounds");
        IRLabel inBounds = newLabel("inBounds");

        if (ARRAY_LENGTH_OFFSET != 0)
            command(new IRBinOpRightConstCommand(temp, array, Operation.Plus, ARRAY_LENGTH_OFFSET));
        else
            command(new IRStoreCommand(temp, array));

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

    @NotNull
    public IRLabel constructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_ctor_" + clazz.name);
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
            return new IRLabel(symbol.instance.name + "_" + symbol.getName());
        } else
            return new IRLabel(symbol.getName());
    }
    //endregion


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
        private boolean isClassScope;

        LocalContext(@NotNull String name, @NotNull RegisterAllocator registerAllocator, @NotNull LabelGenerator labelGenerator) {
            this.name = name;
            this.registerAllocator = registerAllocator;
            this.labelGenerator = labelGenerator;
        }

        LocalContext(@NotNull String name) {
            this(name, new SimpleRegisterAllocator(), new SimpleLabelGenerator());
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

        void asClassScope() {
            isClassScope = true;
        }

        boolean isClassScope() {
            return isClassScope;
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
}
