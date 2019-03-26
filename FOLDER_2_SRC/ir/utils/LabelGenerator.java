package ir.utils;


import ir.commands.flow.IRLabel;

/**
 * Abstraction for label generation
 */
@FunctionalInterface
public interface LabelGenerator {
    /**
     * create a new {@link IRLabel} with the following description.
     */
    IRLabel newLabel(String description);
}
