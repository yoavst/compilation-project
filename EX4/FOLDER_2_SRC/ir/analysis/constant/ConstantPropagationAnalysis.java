package ir.analysis.constant;

import ir.analysis.Analysis;
import ir.commands.IRCommand;
import ir.commands.IRNopCommand;
import ir.commands.arithmetic.*;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRIfNotZeroCommand;
import ir.commands.flow.IRIfZeroCommand;
import ir.commands.functions.IRPushCommand;
import ir.commands.functions.IRPushConstCommand;
import ir.registers.Register;
import utils.NotNull;
import utils.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;

/**
 * Liveness analysis for set of blocks.
 */
public class ConstantPropagationAnalysis extends Analysis<Map<Register, @Nullable Integer>> {
    private static final Map<Register, @Nullable Integer> DEFAULT_VALUE = Collections.emptyMap();

    public ConstantPropagationAnalysis() {
        super(true, DEFAULT_VALUE, Collections.emptyMap());
    }

    @NotNull
    @Override
    protected Map<Register, Integer> transfer(@NotNull IRCommand command, @NotNull Map<Register, @Nullable Integer> old) {
        if (!command.canBeOptimized()) {
            return old;
        } else {
            Map<Register, Integer> newOne = new HashMap<>(old);
            if (command instanceof IRSetValueCommand) {
                IRSetValueCommand c = ((IRSetValueCommand) command);
                if (c.dest.equals(c.source)) {
                    return old;
                } else {
                    newOne.remove(c.dest);
                    if (old.containsKey(c.source))
                        newOne.put(c.dest, old.get(c.source));
                }
            } else if (command instanceof IRConstCommand) {
                IRConstCommand c = ((IRConstCommand) command);
                newOne.put(c.dest, c.value);
            } else if (command instanceof IRBinOpRightConstCommand) {
                IRBinOpRightConstCommand c = (IRBinOpRightConstCommand) command;
                if (c.op == Operation.StrEquals || c.op == Operation.Concat)
                    return old;

                if (old.containsKey(c.first)) {
                    Integer value = old.get(c.first);
                    if (value == null)
                        newOne.put(c.dest, null);
                    else {
                        try {
                            newOne.put(c.dest, Operation.evaluate(value, c.op, c.second));
                        } catch (ArithmeticException e) {
                            // division by zero, do nothing right now.
                        }
                    }
                } else {
                    newOne.put(c.dest, null);
                }
            } else if (command instanceof IRBinOpCommand) {
                IRBinOpCommand c = (IRBinOpCommand) command;
                if (c.op == Operation.StrEquals || c.op == Operation.Concat)
                    return old;

                if (old.containsKey(c.first) && old.containsKey(c.second)) {
                    Integer l = old.get(c.first);
                    Integer r = old.get(c.second);

                    if (l == null || r == null)
                        newOne.put(c.dest, null);
                    else {
                        try {
                            newOne.put(c.dest, Operation.evaluate(l, c.op, r));
                        } catch (ArithmeticException e) {
                            // division by zero, do nothing right now.
                        }
                    }
                } else {
                    newOne.put(c.dest, null);
                }
            } else {
                command.getInvalidates().forEach(newOne::remove);
            }

            return newOne;
        }
    }

    @NotNull
    @Override
    protected Map<Register, Integer> join(@NotNull Map<Register,  @Nullable Integer> v1, @NotNull Map<Register,  @Nullable Integer> v2) {
        Map<Register, Integer> newMap = new HashMap<>(v1);
        v2.forEach((register, value) -> {
            if (newMap.containsKey(register)) {
                newMap.put(register, null);
            } else {
                newMap.put(register, value);
            }
        });
        return newMap;
    }

    public boolean constantPropagation() {
        final boolean[] hasChanged = {false};
        runner.in().forEach((block, info) -> {
            ListIterator<IRCommand> iterator = block.commands.listIterator();
            for (int i = 0; iterator.hasNext(); i++) {
                IRCommand command = iterator.next();
                Map<Register, Integer> mappings = info.get(i);
                if (command.getDependencies().stream().anyMatch(reg -> mappings.get(reg) != null)) {
                    hasChanged[0] = true;
                    System.out.println("Replaced!");
                    // can do replacement
                    if (command instanceof IRBinOpCommand) {
                        IRBinOpCommand c = (IRBinOpCommand) command;
                        if (mappings.get(c.second) != null) {
                            if (mappings.get(c.first) != null) {
                                iterator.set(new IRConstCommand(c.dest, Operation.evaluate(mappings.get(c.first), c.op, mappings.get(c.second))));
                            } else {
                                iterator.set(new IRBinOpRightConstCommand(c.dest, c.first, c.op, mappings.get(c.second)));
                            }
                        } else if (mappings.get(c.first) != null) {
                            switch (c.op) {
                                case Plus:
                                case Times:
                                case Equals:
                                case BoundedPlus:
                                case BoundedTimes:
                                    iterator.set(new IRBinOpRightConstCommand(c.dest, c.second, c.op, mappings.get(c.first)));
                                    break;
                                case GreaterThan:
                                    iterator.set(new IRBinOpRightConstCommand(c.dest, c.second, Operation.LessThan, mappings.get(c.first)));
                                    break;
                                case LessThan:
                                    iterator.set(new IRBinOpRightConstCommand(c.dest, c.second, Operation.GreaterThan, mappings.get(c.first)));
                                    break;
                            }
                        }
                    } else if (command instanceof IRBinOpRightConstCommand) {
                        IRBinOpRightConstCommand c = (IRBinOpRightConstCommand) command;
                        iterator.set(new IRConstCommand(c.dest, Operation.evaluate(mappings.get(c.first), c.op, c.second)));
                    } else if (command instanceof IRSetValueCommand) {
                        IRSetValueCommand c = (IRSetValueCommand) command;
                        iterator.set(new IRConstCommand(c.dest, mappings.get(c.source)));
                    } else if (command instanceof IRIfNotZeroCommand) {
                        IRIfNotZeroCommand c = (IRIfNotZeroCommand) command;
                        int value = mappings.get(c.condition);
                        if (value != 0) {
                            iterator.set(new IRGotoCommand(c.getLabel()));
                        } else {
                            iterator.set(new IRNopCommand());
                        }
                    } else if (command instanceof IRIfZeroCommand) {
                        IRIfZeroCommand c = (IRIfZeroCommand) command;
                        int value = mappings.get(c.condition);
                        if (value == 0) {
                            iterator.set(new IRGotoCommand(c.getLabel()));
                        } else {
                            iterator.set(new IRNopCommand());
                        }
                    } else if (command instanceof IRPushCommand) {
                        IRPushCommand c = (IRPushCommand) command;
                        iterator.set(new IRPushConstCommand(mappings.get(c.source)));
                    }
                }
            }
        });
        return hasChanged[0];
    }
}
