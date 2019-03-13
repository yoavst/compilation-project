package ir.analysis.copy;

import ir.analysis.Analysis;
import ir.commands.IRCommand;
import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.IRBinOpRightConstCommand;
import ir.commands.arithmetic.IRSetValueCommand;
import ir.commands.flow.IRIfNotZeroCommand;
import ir.commands.flow.IRIfZeroCommand;
import ir.commands.functions.IRCallRegisterCommand;
import ir.commands.functions.IRPushCommand;
import ir.commands.memory.IRLoadCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.LocalRegister;
import ir.registers.ParameterRegister;
import ir.registers.Register;
import utils.NotNull;
import utils.Nullable;

import java.util.*;

/**
 * copy propagation analysis for set of blocks.
 */
public class CopyPropagationAnalysis extends Analysis<Map<Register, @Nullable Register>> {
    private static final Map<Register, @Nullable Register> DEFAULT_VALUE = new HashMap<>();

    static {
        // put value to all local registers
        for (int i = 0; i < 100; i++) {
            Register r = new LocalRegister(i);
            DEFAULT_VALUE.put(r, r);
        }
        // put value to all parameters registers
        for (int i = 0; i < 100; i++) {
            Register r = new ParameterRegister(i);
            DEFAULT_VALUE.put(r, r);
        }
    }

    public CopyPropagationAnalysis() {
        super(true, DEFAULT_VALUE, Collections.emptyMap());
    }

    @NotNull
    @Override
    protected Map<Register, @Nullable Register> transfer(@NotNull IRCommand command, @NotNull Map<Register, @Nullable Register> old) {
        if (!command.canBeOptimized()) {
            return old;
        } else if (command instanceof IRSetValueCommand) {
            IRSetValueCommand c = ((IRSetValueCommand) command);
            if (c.dest.equals(c.source) || c.dest instanceof LocalRegister) {
                return old;
            } else {
                Map<Register, @Nullable Register> newMap = new HashMap<>(old);

                if (old.containsKey(c.dest)) {
                    // need to invalidate
                    newMap.remove(c.dest);
                }
                // add to map
                newMap.put(c.dest, c.source);
                return newMap;
            }
        } else {
            // need to invalidate the invalidated
            Map<Register, @Nullable Register> newMap = new HashMap<>(old);
            for (Register invalidated : command.getInvalidates()) {
                newMap.put(invalidated, null);
                // remove all reference to invalidated
                newMap.entrySet().removeIf(e -> invalidated.equals(e.getValue()));
            }
            return newMap;
        }
    }

    @NotNull
    @Override
    protected Map<Register, Register> join(@NotNull Map<Register, @Nullable Register> v1, @NotNull Map<Register, @Nullable Register> v2) {
        Map<Register, Register> newMap = new HashMap<>(v1);
        newMap.putAll(v2);

        Set<Register> invalidated = new HashSet<>(v1.keySet());
        invalidated.retainAll(v2.keySet());
        for (Register register : invalidated) {
            Register first = v1.get(register), second = v2.get(register);
            if (first == null || !first.equals(second)) {
                // has two mapping, need to be invalidated
                newMap.put(register, null);
            }
        }
        return newMap;
    }

    public boolean copyPropagation() {
        final boolean[] hasChanged = {false};
        runner.in().forEach((block, info) -> {
            ListIterator<IRCommand> iterator = block.commands.listIterator();
            for (int i = 0; iterator.hasNext(); i++) {
                IRCommand command = iterator.next();
                Map<Register, Register> mappings = info.get(i);
                mappings.entrySet().removeIf(e -> e.getValue() == null);

                if (command.getDependencies().stream().anyMatch(mappings::containsKey)) {
                    hasChanged[0] = true;
                    // can do replacement
                    if (command instanceof IRBinOpCommand) {
                        IRBinOpCommand c = (IRBinOpCommand) command;
                        iterator.set(new IRBinOpCommand(c.dest, mappings.getOrDefault(c.first, c.first), c.op, mappings.getOrDefault(c.second, c.second)));
                    } else if (command instanceof IRBinOpRightConstCommand) {
                        IRBinOpRightConstCommand c = (IRBinOpRightConstCommand) command;
                        iterator.set(new IRBinOpRightConstCommand(c.dest, mappings.getOrDefault(c.first, c.first), c.op, c.second));
                    } else if (command instanceof IRSetValueCommand) {
                        IRSetValueCommand c = (IRSetValueCommand) command;
                        iterator.set(new IRSetValueCommand(c.dest, mappings.getOrDefault(c.source, c.source)));
                    } else if (command instanceof IRIfNotZeroCommand) {
                        IRIfNotZeroCommand c = (IRIfNotZeroCommand) command;
                        iterator.set(new IRIfNotZeroCommand(mappings.getOrDefault(c.condition, c.condition), c.getLabel()));
                    } else if (command instanceof IRIfZeroCommand) {
                        IRIfZeroCommand c = (IRIfZeroCommand) command;
                        iterator.set(new IRIfZeroCommand(mappings.getOrDefault(c.condition, c.condition), c.getLabel()));
                    } else if (command instanceof IRCallRegisterCommand) {
                        IRCallRegisterCommand c = (IRCallRegisterCommand) command;
                        iterator.set(new IRCallRegisterCommand(mappings.getOrDefault(c.function, c.function)));
                    } else if (command instanceof IRPushCommand) {
                        IRPushCommand c = (IRPushCommand) command;
                        iterator.set(new IRPushCommand(mappings.getOrDefault(c.source, c.source)));
                    } else if (command instanceof IRLoadCommand) {
                        IRLoadCommand c = (IRLoadCommand) command;
                        iterator.set(new IRLoadCommand(c.dest, mappings.getOrDefault(c.source, c.source)));
                    } else if (command instanceof IRStoreCommand) {
                        IRStoreCommand c = (IRStoreCommand) command;
                        iterator.set(new IRStoreCommand(c.dest, mappings.getOrDefault(c.source, c.source)));
                    }
                }
            }
        });
        return hasChanged[0];
    }
}
