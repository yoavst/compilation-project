package ir.utils;


import ir.flow.IRLabel;

@FunctionalInterface
public interface LabelGenerator {
    /**
     * create a new {@link IRLabel} with the following description.
     */
    IRLabel newLabel(String description);
}
