package ir.utils;

import ir.flow.IRLabel;
import utils.Nullable;

/**
 * Simple count
 */
public class SimpleLabelGenerator implements LabelGenerator {
    private static int counter = 0;
    @Nullable
    private final String prefix;

    public SimpleLabelGenerator(@Nullable String prefix) {
        this.prefix = prefix;
    }

    public SimpleLabelGenerator() {
        this(null);
    }

    @Override
    public IRLabel newLabel(String description) {
        return new IRLabel("_" + (prefix != null ? prefix + "_" : "") + description + "_" + counter++);
    }
}
