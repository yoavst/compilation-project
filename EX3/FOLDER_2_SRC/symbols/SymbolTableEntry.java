package symbols;

import types.Type;

class SymbolTableEntry {
    /**
     * Index in the symbol table, the hash result on the name.
     */
    final int index;
    final String name;
    final Type type;

    /**
     * The symbol table also remembers the order of insertions. This field points to the last inserted value.
     */
    SymbolTableEntry prev;
    /**
     * The symbol table uses hashmap with chaining. This field points to the next value in the chain.
     */
    SymbolTableEntry next;

    /** The prevtop_index is just for debug purposes ... */
    final int prevtop_index;

    /**
     * Whether this is a variable declaration or a type/function declaration.
     */
    final boolean isVariableDeclaration;

    /**
     * There are three types of scope: Class/ClassScan, Function, Block
     * Class and Function scopes are considered enclosing scope, and have special rules for resolving.
     * <br /><br />
     * The rules for this field are the following:
     * <ul>
     *     <li>For every block scope, increase value by 10.</li>
     *     <li>For every enclosing scope, increase value by 1.</li>
     * </ul>
     * @see types.TYPE_FOR_SCOPE_BOUNDARIES.Scope
     */
    final int scopeMajor;

    SymbolTableEntry(
            String name,
            Type type,
            int index,
            SymbolTableEntry next,
            SymbolTableEntry prev,
            int prevtop_index,
            boolean isVariableDeclaration,
            int scopeMajor) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.next = next;
        this.prev = prev;
        this.prevtop_index = prevtop_index;
        this.isVariableDeclaration = isVariableDeclaration;
        this.scopeMajor = scopeMajor;
    }
}
