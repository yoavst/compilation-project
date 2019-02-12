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
public class ClassTable {
    private static final int OFFSET_CHANGE = 4;
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


    public ClassTable(@NotNull TypeClass type, @Nullable ClassTable parentTable, int initialVtableOffset, int initialFieldOffset) {
        this.type = type;
        this.parentTable = parentTable;

        if (parentTable == null) {
            functionsLastOffset = initialVtableOffset;
            fieldsLastOffset = initialFieldOffset;
        } else {
            functionsLastOffset = parentTable.functionsLastOffset;
            fieldsLastOffset = parentTable.fieldsLastOffset;
        }
    }

    /**
     * Will put the symbol to the virtual table, override the function if already exists.
     */
    public void insertFunction(@NotNull Symbol symbol) {
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
    public void insertField(@NotNull Symbol symbol) {
        assert type.equals(symbol.instance);
        assert getField(symbol) < 0;
        assert symbol.isField();


        fieldsOffsets.put(symbol, fieldsLastOffset);
        fieldsLastOffset += type.size();
    }

    /**
     * get the offset for the function on the vtable, or -1 if missing
     */
    public int getFunction(@NotNull Symbol symbol) {
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
     * get the offset from the object beginning  or -1 if missing
     */
    public int getField(@NotNull Symbol symbol) {
        assert type.isAssignableFrom(symbol.instance);

        if (functionOffsets.containsKey(symbol)) {
            return fieldsOffsets.get(symbol);
        } else if (parentTable != null) {
            return parentTable.getField(symbol);
        } else {
            return -1;
        }
    }
}
