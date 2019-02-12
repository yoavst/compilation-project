package ir;

import ir.flow.IRLabel;
import ir.functions.IRLabelMark;
import ir.registers.ConstantResourceRegister;
import ir.registers.Register;
import symbols.Symbol;
import types.TypeClass;
import types.builtins.TypeArray;
import utils.NotNull;
import utils.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public final class IRContext {
    public static final int NilValue = 100_000;
    public static final int VTABLE_POSITION = 4;
    public static final IRLabel MallocCommand = new IRLabel("_Malloc");
    private final HashSet<@NotNull Integer> usedRegisters = new HashSet<>(100);
    private final List<@NotNull IRCommand> commands = new ArrayList<>();

    private final HashMap<@NotNull String, @NotNull ConstantResourceRegister> preallocatedStrings = new HashMap<>();
    private int preallocateCounter = 0;

    private int labelCounter = 0;

    //region Registers
    public Register getNewRegister() {
        int i = 1;
        while (usedRegisters.contains(i)) {
            i++;
        }
        usedRegisters.add(i);
        return new Register(i);
    }

    public void freeRegister(Register register) {
        usedRegisters.remove(register.getId());
    }

    public ConstantResourceRegister preallocateString(String s) {
        if (preallocatedStrings.containsKey(s)) {
            return preallocatedStrings.get(s);
        }

        int counter = preallocateCounter++;
        ConstantResourceRegister register = new ConstantResourceRegister(counter);
        preallocatedStrings.put(s, register);
        return register;
    }
    //endregion

    public void addCommand(@NotNull IRCommand command) {
        commands.add(command);
    }

    public void putLabel(@NotNull IRLabel label) {
        commands.add(new IRLabelMark(label));
    }


    //region Symbols
    @NotNull
    public IRLabel constructorOf(@NotNull TypeClass clazz) {
        return new IRLabel("_ctor_" + clazz.name);
    }

    @NotNull
    public IRLabel constructorOf(@NotNull TypeArray array) {
        return new IRLabel("_ctor_array_" + array.arrayType.name);
    }

    @NotNull
    public IRLabel getReturnLabel(@NotNull Symbol symbol) {
        return new IRLabel("_return_" + symbol.toString());
    }

    @NotNull
    public IRLabel generateLabel(@Nullable String prefix) {
        return new IRLabel("_" + (prefix != null ? prefix : "label") + labelCounter++);
    }

    /**
     * Get the IR label for a global function symbol
     */
    @NotNull
    public IRLabel getFunction(@NotNull Symbol symbol) {
        assert !symbol.isBounded();
        // FIXME implement it
        throw new IllegalStateException();
    }

    @NotNull
    public VTable getVirtualTable(@NotNull TypeClass typeClass) {
        // FIXME implement it
        throw new IllegalStateException();
    }

    //endregion
}
