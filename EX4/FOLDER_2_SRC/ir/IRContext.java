package ir;

import ir.flow.IRLabel;
import types.TypeClass;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public final class IRContext {
    public static final int NilValue = 100_000;
    public static final IRLabel MallocCommand = new IRLabel("_Malloc");
    private final HashSet<Integer> usedRegisters = new HashSet<>(100);
    private final List<IRCommand> commands = new ArrayList<>();
    private final HashMap<String, Integer> preallocatedStrings = new HashMap<>();
    private int preallocateCounter = 0;

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

    public int preallocateString(String s) {
        if (preallocatedStrings.containsKey(s)) {
            return preallocatedStrings.get(s);
        }

        int counter = preallocateCounter++;
        preallocatedStrings.put(s, counter);
        return counter;
    }

    public IRLabel constructorOf(TypeClass clazz) {
        return new IRLabel("_ctor_" + clazz.name);
    }

    public void addCommand(IRCommand command) {
        commands.add(command);
    }
}
