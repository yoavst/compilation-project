package ir.utils;

import symbols.Symbol;
import types.TypeClass;
import utils.NotNull;
import utils.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Virtual table and fields table for a class
 */
final class ClassTable {
    private static final int OFFSET_CHANGE = 4;
    private static int current_id = 1;
    @Nullable
    private ClassTable parentTable;
    @NotNull
    private Map<@NotNull Symbol, @NotNull Integer> functionOffsets = new HashMap<>();
    @NotNull
    private Map<@NotNull Symbol, @NotNull Integer> fieldsOffsets = new HashMap<>();
    @NotNull
    private TypeClass type;
    private int functionsLastOffset;
    private int fieldsLastOffset;
    public final int id;


    ClassTable(@NotNull TypeClass type, @Nullable ClassTable parentTable, int initialVtableOffset, int initialFieldOffset) {
        this.type = type;
        this.parentTable = parentTable;

        if (parentTable == null) {
            functionsLastOffset = initialVtableOffset;
            fieldsLastOffset = initialFieldOffset;
        } else {
            functionsLastOffset = parentTable.functionsLastOffset;
            fieldsLastOffset = parentTable.fieldsLastOffset;
        }
        id = current_id++;
    }

    /**
     * Will put the symbol to the virtual table, override the function if already exists.
     */
    void insertFunction(@NotNull Symbol symbol) {
        assert type.equals(symbol.instance);
        assert symbol.isFunction();

        int offset = getFunction(symbol);
        if (offset < 0) {
            // not exist
            functionOffsets.put(symbol, functionsLastOffset);
            functionsLastOffset += OFFSET_CHANGE;
        } else {
            // exist, should override
            functionOffsets.put(symbol, offset);
        }
    }

    /**
     * Will save the field's offset, throws if already exists.
     */
    void insertField(@NotNull Symbol symbol) {
        assert type.equals(symbol.instance);
        assert hasField(symbol);
        assert symbol.isField();


        fieldsOffsets.put(symbol, fieldsLastOffset);
        fieldsLastOffset += IRContext.PRIMITIVE_DATA_SIZE;
    }

    /**
     * get the offset for the function on the vtable, or -1 if missing
     */
    int getFunction(@NotNull Symbol symbol) {
        assert type.isAssignableFrom(symbol.instance);

        if (functionOffsets.containsKey(symbol)) {
            return functionOffsets.get(symbol);
        } else if (parentTable != null) {
            return parentTable.getFunction(symbol);
        } else {
            return -1;
        }
    }

    /**
     * get the offset from the object beginning
     */
    int getField(@NotNull Symbol symbol) {
        assert type.isAssignableFrom(symbol.instance);

        if (fieldsOffsets.containsKey(symbol)) {
            return fieldsOffsets.get(symbol);
        } else if (parentTable != null) {
            return parentTable.getField(symbol);
        } else {
            throw new IllegalArgumentException("cannot find the field: " + symbol);
        }
    }
    /**
     * check if has field
     */
    boolean hasField(@NotNull Symbol symbol) {
        assert type.isAssignableFrom(symbol.instance);

        if (fieldsOffsets.containsKey(symbol)) {
            return true;
        } else if (parentTable != null) {
            return parentTable.hasField(symbol);
        } else {
            return false;
        }
    }


    int getTotalSize() {
        return fieldsLastOffset;
    }

    Map<Symbol, Integer> getFullVtable() {
        Map<Symbol, Integer> vtable = new HashMap<>();
        ClassTable instance = this;
        while (instance != null) {
            for (Map.Entry<Symbol, Integer> entry : instance.functionOffsets.entrySet()) {
                if (!vtable.containsKey(entry.getKey()))
                    vtable.put(entry.getKey(), entry.getValue());
            }
            instance = instance.parentTable;
        }
        return vtable;
    }
}
