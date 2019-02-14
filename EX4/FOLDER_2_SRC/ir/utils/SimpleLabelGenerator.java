package ir.utils;

import ir.commands.flow.IRLabel;
import utils.Nullable;

/**
 * Simple counter implementation for label generation
 */
public class SimpleLabelGenerator implements LabelGenerator {
    private static int counter = 0;
    @Nullable
    private final String prefix;

    private SimpleLabelGenerator(@Nullable String prefix) {
        this.prefix = prefix;
    }

    SimpleLabelGenerator() {
        this(null);
    }

    @Override
    public IRLabel newLabel(String description) {
        return new IRLabel("_" + (prefix != null ? prefix + "_" : "") + description + "_" + counter++);
    }
}
