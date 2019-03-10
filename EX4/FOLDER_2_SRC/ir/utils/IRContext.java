package ir.utils;

import ir.commands.IRCommand;
import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRConstCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRIfZeroCommand;
import ir.commands.flow.IRLabel;
import ir.commands.functions.IRCallCommand;
import ir.commands.functions.IRPopCommand;
import ir.commands.functions.IRPushCommand;
import ir.commands.memory.IRLoadAddressFromLabelCommand;
import ir.commands.memory.IRLoadCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.GlobalRegister;
import ir.registers.LocalRegister;
import ir.registers.ParameterRegister;
import ir.registers.Register;
import symbols.Symbol;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import types.builtins.TypeVoid;
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
    public static final int MAX_INT = 32767;
    public static final int MIN_INT = -32768;

    private static final int OBJECT_FIELDS_INITIAL_OFFSET = 8;
    private static final int VIRTUAL_TABLE_INITIAL_OFFSET = 0;
    private static final int ID_OFFSET_IN_OBJECT = 0;

    public static final IRLabel STDLIB_FUNCTION_MAIN = new IRLabel("main").startingLabel();
    public static final IRLabel STDLIB_FUNCTION_PRINT_INT = generateFunctionLabelFor(new Symbol("PrintInt", new TypeFunction("PrintInt", TypeVoid.instance, Utils.mutableListOf(TypeInt.instance))));
    public static final IRLabel STDLIB_FUNCTION_PRINT_STRING = generateFunctionLabelFor(new Symbol("PrintString", new TypeFunction("PrintString", TypeVoid.instance, Utils.mutableListOf(TypeString.instance))));
    public static final IRLabel STDLIB_FUNCTION_PRINT_TRACE = generateFunctionLabelFor(new Symbol("PrintTrace", new TypeFunction("PrintTrace", TypeVoid.instance)));

    public static final IRLabel STDLIB_FUNCTION_THROW_NULL = new IRLabel("___throwNull___").startingLabel();
    public static final IRLabel STDLIB_FUNCTION_THROW_DIVISION_BY_ZERO = new IRLabel("___throwDivisionByZero___").startingLabel();
    public static final IRLabel STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS = new IRLabel("___throwOutOfBounds___").startingLabel();
    public static final IRLabel STDLIB_FUNCTION_MALLOC = new IRLabel("___malloc___").startingLabel();
    public static final IRLabel STDLIB_FUNCTION_EXIT = new IRLabel("___exit___").startingLabel();
    public static final Symbol MAIN_SYMBOL = new Symbol("main", new TypeFunction("main"));
    public static final Register FIRST_FUNCTION_PARAMETER = new ParameterRegister(0);

    private final List<@NotNull LocalContext> localsStack = new ArrayList<>();
    private final Map<@NotNull String, @NotNull IRLabel> strings = new HashMap<>();
    private final Map<@NotNull TypeClass, @NotNull ClassTable> classTables = new HashMap<>();
    private final List<@NotNull IRLabel> preMainFunctions = new ArrayList<>();
    private List<@NotNull IRCommand> commands = new ArrayList<>();

    private int loadedFields = 0;

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

    /**
     * Returns the number of fields loaded since the last reset. Used to let functions report how many locals they need.
     */
    public int getLoadedFieldsCount() {
        return loadedFields;
    }

    /**
     * Resets loaded fields counter. Usually done on starting a function.
     */
    public void resetLoadedFieldsCounter() {
        loadedFields = 0;
    }

    /**
     * Returns the current local context, assuming there is one
     */
    @NotNull
    private LocalContext currentContext() {
        return last(localsStack);
    }

    /**
     * Adding a pre-main hook. The label will be treated as a function, and will be called once, and before calling to actual main.
     */
    public void addPreMainFunction(@NotNull IRLabel label) {
        preMainFunctions.add(label);
    }

    /**
     * Returns all the pre-main hooks.
     */
    @NotNull
    public List<IRLabel> getPreMainFunctions() {
        return preMainFunctions;
    }

    /**
     * Opens a new scope, and loading the symbols into it by binding them to registers, based on the scope type.
     *
     * @param name         The name of the scope for debug purposes
     * @param symbols      The newly loaded symbols
     * @param type         The type of the scope
     * @param isParameters whether to treat the symbols as function parameters
     * @param isBounded    is this a bounded scope.
     * @see ScopeType
     */
    private static int localCounter = 0;
    public void openScope(@NotNull String name, @NotNull List<Symbol> symbols, ScopeType type, boolean isParameters, boolean isBounded) {
        // use the parent register allocator if exists
        RegisterAllocator allocator = localsStack.isEmpty() ? new SimpleRegisterAllocator() : last(localsStack).registerAllocator;

        LocalContext context = new LocalContext(name, allocator, new SimpleLabelGenerator(), type);

        if (type != ScopeType.Inner) {
            localCounter = 0;
        }
        // In case of function parameters for a bounded function, the first parameter will be [this], so the first register should be skipped.
        if (type == ScopeType.Function && isBounded && isParameters) {
            localCounter++;
        }

        // load symbols
        for (Symbol symbol : symbols) {
            if (symbol.isFunction()) {
                context.addFunction(symbol, generateFunctionLabelFor(symbol));
            } else if (isParameters) {
                context.addLocal(symbol, new ParameterRegister(localCounter++));
            } else if (type == ScopeType.Global) {
                context.addLocal(symbol, new GlobalRegister(localCounter++));
            } else {
                context.addLocal(symbol, new LocalRegister(localCounter++));
                loadedFields++;
            }
        }

        // save the changes
        localsStack.add(context);
    }

    /**
     * Opens object scope by recursively opening parent class scope and then open the object's scope.
     * The method will load all the necessary symbols.
     */
    public void openObjectScope(@NotNull TypeClass clazz) {
        if (clazz.parent != null)
            openObjectScope(clazz.parent);

        List<Symbol> addedSymbols = clazz.getFields();
        addedSymbols.addAll(clazz.getMethods());
        openScope(clazz.name, addedSymbols, ScopeType.Class, false, true);
    }

    /**
     * Close object scope opened by {@link #openObjectScope(TypeClass)}.
     */
    public void closeObjectScope() {
        while (currentContext().isClassScope())
            closeScope();
    }

    /**
     * Close single scope, resetting register allocator if existing an allocation scope (currently only function is allocation scope).
     */
    public void closeScope() {
        LocalContext last = currentContext();
        if (last.scopeType == ScopeType.Function)
            last.registerAllocator.freeAll();

        localsStack.remove(localsStack.size() - 1);
    }

    /**
     * Returns the register assigned to the given symbol, throwing exception if missing.
     */
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

    /**
     * Returns the function label assigned for the given function symbol, throwing exception if missing.
     */
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

    //region Codegen

    /**
     * Generate code of checking if the value in the register is not null
     */
    public void checkNotNull(@NotNull Register register) {
        Register temp = newRegister();
        IRLabel notNull = newLabel("notnull");

        command(new IRBinOpRightConstCommand(temp, register, Operation.Equals, NIL_VALUE)); // temp = register == Nil
        command(new IRIfZeroCommand(temp, notNull));                                        // if not temp jump notnull
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_NULL));                             // call __throw_null
        label(notNull);                                                                     // notnull:
    }

    /**
     * Generate code of checking if the value in the register is not null
     */
    public void checkNotNullForArray(@NotNull Register register) {
        Register temp = newRegister();
        IRLabel notNull = newLabel("notnull");

        command(new IRBinOpRightConstCommand(temp, register, Operation.Equals, NIL_VALUE));          // temp = register == Nil
        command(new IRIfZeroCommand(temp, notNull));                                                 // if not temp jump notnull
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS));                             // call __throw_array_out_of_bounds
        label(notNull);                                                                              // notnull:
    }

    /**
     * Generate code of checking if array access is within bounds
     */
    public void checkLength(@NotNull Register array, @NotNull Register index) {
        Register temp = newRegister();
        IRLabel outOfBounds = newLabel("outOfBounds"), inBounds = newLabel("inBounds");


        command(new IRBinOpRightConstCommand(temp, array, Operation.Plus, ARRAY_LENGTH_OFFSET));    // temp = array + offset_for_length_field
        command(new IRLoadCommand(temp, temp));                                                     // temp = *temp (array.length)
        command(new IRBinOpCommand(temp, temp, Operation.GreaterThan, index));                      // temp = temp > index
        command(new IRIfZeroCommand(temp, outOfBounds));                                            // if temp == 0 goto outOfBounds
        command(new IRBinOpRightConstCommand(temp, index, Operation.GreaterThan, -1));      // temp = index > -1
        command(new IRIfZeroCommand(temp, outOfBounds));                                            // if temp == 0 goto outOfBounds
        command(new IRGotoCommand(inBounds));                                                       // goto inBounds
        label(outOfBounds);                                                                         // outOfBounds:
        command(new IRCallCommand(STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS));                            // call __throw_array_out_of_bounds
        label(inBounds);                                                                            // inBounds:
    }

    /**
     * Generates code of calling malloc.
     *
     * @param size Register holding the size to allocate
     * @return Register with allocated address
     */
    public Register malloc(@NotNull Register size) {
        Register temp = newRegister();

        command(new IRPushCommand(size));                       // push size
        command(new IRCallCommand(STDLIB_FUNCTION_MALLOC));     // call __malloc
        command(new IRPopCommand(temp));                        // pop temp
        return temp;
    }

    /**
     * Generates code of exiting the program
     */
    public void exit() {
        command(new IRCallCommand(STDLIB_FUNCTION_EXIT));       // call __exit
    }


    /**
     * Generates code of assigning the virtual table address for a newly created class.
     *
     * @param thisReg Register holding an address to object
     * @param clazz   The object's class
     */
    public void assignVirtualTable(@NotNull Register thisReg, @NotNull TypeClass clazz) {
        Register offseted = newRegister(), temp = newRegister();

        command(new IRBinOpRightConstCommand(offseted, thisReg, Operation.Plus, VIRTUAL_TABLE_OFFSET_IN_OBJECT));   // offseted = this + vtable_field_offset
        command(new IRLoadAddressFromLabelCommand(temp, generateVirtualTableLabelFor(clazz)));                             // temp = [vtable_of_class]
        command(new IRStoreCommand(offseted, temp));                                                                // *offseted = temp
        command(new IRBinOpRightConstCommand(offseted, thisReg, Operation.Plus, ID_OFFSET_IN_OBJECT));              // offseted = this + id_field_offset
        command(new IRConstCommand(temp, classTables.get(clazz).id));                                               // temp = class_unique_id
        command(new IRStoreCommand(offseted, temp));                                                                // *offseted = temp
    }
    //endregion

    //region Commands

    /**
     * Allocate and return a new temporary register to be used.
     */
    @NotNull
    public Register newRegister() {
        return currentContext().registerAllocator.newRegister();
    }

    /**
     * Create new label using the description as hint for naming.
     */
    @NotNull
    public IRLabel newLabel(@Nullable String description) {
        return currentContext().labelGenerator.newLabel(description);
    }

    /**
     * Writes a command into the IR file
     */
    public void command(@NotNull IRCommand command) {
        commands.add(command);
    }

    /**
     * Adds label into the IR file.
     */
    public void label(@NotNull IRLabel label) {
        command(label);
    }

    /**
     * Returns label for constructor of the given class.
     */
    @NotNull
    public IRLabel constructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_ctor_" + clazz.name).startingLabel();
    }

    /**
     * Returns label for the internal constructor of the given class.
     */
    @NotNull
    public IRLabel internalConstructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_internal_ctor_" + clazz.name).startingLabel();
    }

    /**
     * Returns label for constructor of the given array
     */
    @NotNull
    public IRLabel constructorOf(@NotNull TypeArray array) {
        return new IRLabel("_ctor_array_" + array.arrayType.name).startingLabel();
    }

    /**
     * Returns label for the return part of a function.
     * Each function has the following form:
     * <p>
     * function:
     * ...
     * _return_function:
     * return
     */
    @NotNull
    public IRLabel returnLabelFor(@NotNull Symbol symbol) {
        return new IRLabel("_return_" + symbol.toString());
    }

    /**
     * Return label for constructor of array.
     */
    @NotNull
    public IRLabel returnLabelForConstructor(@NotNull TypeArray array) {
        return new IRLabel("_return_ctor_array_" + array.arrayType.name);
    }

    /**
     * Return label for constructor of class.
     */
    @NotNull
    public IRLabel returnLabelForConstructor(@NotNull TypeClass clazz) {
        return new IRLabel("_return_ctor_" + clazz.name);
    }

    /**
     * Return label for constructor of class.
     */
    @NotNull
    public IRLabel returnLabelForInternalConstructor(@NotNull TypeClass clazz) {
        return new IRLabel("_return_internal_ctor_" + clazz.name);
    }

    /**
     * Return label for pre main hook
     */
    @NotNull
    public IRLabel returnLabelForPreMainFunction(@NotNull String fieldName) {
        return new IRLabel("_return_pre_main_hook_" + fieldName);
    }

    public static boolean isReturnLabel(@Nullable IRLabel label) {
        return label != null && label.toString().startsWith("_return_");
    }

    /**
     * Generates label for a symbol function
     */
    @NotNull
    private static IRLabel generateFunctionLabelFor(@NotNull Symbol symbol) {
        if (symbol.isBounded() && symbol.instance != null) {
            return new IRLabel("_f_" + symbol.instance.name + "_" + symbol.getName()).startingLabel();
        } else
            return new IRLabel("_f_" + symbol.getName()).startingLabel();
    }

    /**
     * Generates label for the virtual table entry of a class
     */
    @NotNull
    private IRLabel generateVirtualTableLabelFor(@NotNull TypeClass clazz) {
        return new IRLabel("_vtable_" + classTables.get(clazz).id + "_" + clazz.name);
    }
    //endregion

    //region Local Context definition

    public enum ScopeType {
        /**
         * Global scope fields are stored on the pre-allocated region in memory.
         */
        Global,
        /**
         * Class fields are stored in the object memory.
         */
        Class,
        /**
         * Function fields are stored on the stack.
         */
        Function,
        /**
         * Inner function fields are also stored on the stack.
         */
        Inner
    }

    /**
     * Represents a local IR context, with its own locals and functions.
     */
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

    //region Output

    /**
     * Returns the commands that were loaded to the file
     */
    @NotNull
    public List<IRCommand> getCommands() {
        return commands;
    }

    public void setCommands(List<IRCommand> commands) {
        this.commands = commands;
    }

    /**
     * Returns the virtual tables with their corresponding label that were loaded to the IR file.
     */
    @NotNull
    public Map<IRLabel, List<IRLabel>> getVirtualTables() {
        Map<IRLabel, List<IRLabel>> labels = new HashMap<>();
        classTables.forEach((clazz, classTable) -> {
            List<IRLabel> vtable = classTable.getFullVtable().stream().map(IRContext::generateFunctionLabelFor).collect(Collectors.toList());
            labels.put(generateVirtualTableLabelFor(clazz), vtable);
        });
        return labels;
    }

    /**
     * Returns the constant strings and their corresponding label that were loaded to the IR file.
     */
    @NotNull
    public Map<String, IRLabel> getConstantStrings() {
        return strings;
    }

    /**
     * Return all the globals with their corresponding registers that were loaded to the IR file
     */
    @NotNull
    public Map<Symbol, Register> getGlobals() {
        Map<Symbol, Register> globals = new HashMap<>();
        LocalContext context = localsStack.get(0);
        for (Symbol symbol : context.locals.keySet()) {
            if (symbol.isField()) {
                Register r = context.locals.get(symbol);
                globals.put(symbol, r);
            }
        }
        return globals;
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
            getVirtualTables().forEach((label, methods) ->
                    result.append(label)
                            .append(" = [")
                            .append(methods.stream().map(IRLabel::toString).collect(Collectors.joining(", ")))
                            .append("];")
                            .append("\r\n"));

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