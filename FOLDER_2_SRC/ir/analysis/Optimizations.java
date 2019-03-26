package ir.analysis;

import ir.analysis.constant.ConstantPropagationAnalysis;
import ir.analysis.copy.CopyPropagationAnalysis;
import ir.analysis.liveness.LivenessAnalysis;
import ir.commands.IRCommand;
import ir.commands.IRNopCommand;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRLabel;
import utils.NotNull;
import utils.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Optimizations {
    private static final int MAX_OPTIMIZATIONS_ROUND = 3;
    private @Nullable IRBlock removed;

    public List<IRCommand> optimize(List<IRCommand> commands) {
        for (int i = 0; i < MAX_OPTIMIZATIONS_ROUND; i++) {
            IRBlockGenerator generator = new IRBlockGenerator();
            for (IRCommand command : commands) {
                generator.handle(command);
            }
            List<IRBlock> blocks = generator.finish();
            // get program functions
            List<IRBlock> functions = blocks.stream().filter(IRBlock::isStartingBlock).collect(Collectors.toList());
            functions.forEach(this::optimize);
            commands = new ArrayList<>();
            for (IRBlock block : blocks) {
                if (!block.isIgnored) {
                    if (block.label != null)
                        commands.add(block.label);

                    commands.addAll(block.commands);
                }
            }

            immidiateGotoRemoval(commands);
        }

        return commands;
    }


    private boolean optimize(IRBlock irBlock) {
        boolean wasOptimized = true;
        int i = 0;
        while (i < 3 && wasOptimized) {
            Set<IRBlock> blocks = irBlock.scanGraph();
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            livenessAnalysis.run(blocks);
            wasOptimized = livenessAnalysis.deadCodeElimination();


            CopyPropagationAnalysis copyPropagationAnalysis = new CopyPropagationAnalysis();
            copyPropagationAnalysis.run(blocks);
            wasOptimized |= copyPropagationAnalysis.copyPropagation();

            ConstantPropagationAnalysis constantPropagationAnalysis = new ConstantPropagationAnalysis();
            constantPropagationAnalysis.run(blocks);
            wasOptimized |= constantPropagationAnalysis.constantPropagation();
            i++;
        }

        Set<IRBlock> blocks = irBlock.scanGraph();
        deadBlockElimination(irBlock, blocks);

        return true;
    }

    private boolean deadBlockElimination(@NotNull IRBlock first, Set<IRBlock> blocks) {
        IRBlock current = first;
        boolean hasChanged = false;
        while (current.realNextBlock != null && blocks.contains(current.realNextBlock)) {
            if (current.realNextBlock.prev.isEmpty()) {
                // dead block - remove it
                removed = current.realNextBlock;
                blocks.remove(removed);
                current.realNextBlock = removed.realNextBlock;
                removed.isIgnored = true;
                hasChanged = true;
                continue;
            }
            current = current.realNextBlock;
        }
        return hasChanged;
    }

    private boolean immidiateGotoRemoval(List<IRCommand> commands) {
        boolean hasChanged = false;
        for (int i = 0; i < commands.size() - 1; i++) {
            IRCommand command = commands.get(i);
            if (command instanceof IRGotoCommand) {
                IRLabel label = ((IRGotoCommand) command).getLabel();
                IRCommand nextCommand = commands.get(i + 1);
                if (label.equals(nextCommand)) {
                    // goto can be removed
                    commands.set(i, new IRNopCommand());
                    hasChanged = true;
                }
            }
        }
        return hasChanged;
    }
}
