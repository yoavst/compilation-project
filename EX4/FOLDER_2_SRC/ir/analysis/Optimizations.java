package ir.analysis;

import ir.analysis.constant.ConstantPropagationAnalysis;
import ir.analysis.copy.CopyPropagationAnalysis;
import ir.analysis.liveness.LivenessAnalysis;
import ir.commands.IRCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Optimizations {
    private static final int MAX_OPTIMIZATIONS_ROUND = 3;

    public List<IRCommand> optimize(List<IRCommand> commands) {
        IRBlockGenerator generator = new IRBlockGenerator();
        for (IRCommand command : commands) {
            generator.handle(command);
        }
        List<IRBlock> blocks = generator.finish();
        // get program functions
        List<IRBlock> functions = blocks.stream().filter(IRBlock::isStartingBlock).collect(Collectors.toList());
        // handle each function
        functions.forEach(this::optimize);

        List<IRCommand> results = new ArrayList<>();
        for (IRBlock block : blocks) {
            if (block.label != null)
                results.add(block.label);

            results.addAll(block.commands);
        }
        return results;
    }

    private void optimize(IRBlock irBlock) {
        Set<IRBlock> blocks = irBlock.scanGraph();
        for (int i = 0; i < MAX_OPTIMIZATIONS_ROUND; i++) {
            LivenessAnalysis livenessAnalysis = new LivenessAnalysis();
            livenessAnalysis.run(blocks);
            boolean wasOptimized = livenessAnalysis.deadCodeElimination();

            CopyPropagationAnalysis copyPropagationAnalysis = new CopyPropagationAnalysis();
            copyPropagationAnalysis.run(blocks);
            wasOptimized |= copyPropagationAnalysis.copyPropagation();

            ConstantPropagationAnalysis constantPropagationAnalysis = new ConstantPropagationAnalysis();
            constantPropagationAnalysis.run(blocks);
            wasOptimized |= constantPropagationAnalysis.constantPropagation();

            if (!wasOptimized)
                break;
        }
    }
}
