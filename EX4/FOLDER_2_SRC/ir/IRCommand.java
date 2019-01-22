package ir;

import utils.NotNull;

/**
 * The base class of IR commands.
 */
public abstract class IRCommand {
    @NotNull
    private final String description;

    public IRCommand(@NotNull String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }

    public String getDescription() {
        return description;
    }
}
