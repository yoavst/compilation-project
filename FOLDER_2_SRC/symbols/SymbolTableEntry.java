package symbols;

import utils.NotNull;

class SymbolTableEntry {
    /**
     * Index in the symbol table, the hash result on the name.
     */
    final int index;
    @NotNull
    public final String name;
    @NotNull
    public final Symbol symbol;

    /**
     * The symbol table also remembers the order of insertions. This field points to the last inserted value.
     */
    SymbolTableEntry prev;
    /**
     * The symbol table uses hashmap with chaining. This field points to the next value in the chain.
     */
    SymbolTableEntry next;

    /**
     * The prevtop_index is just for debug purposes ...
     */
    final int prevtop_index;

    /**
     * Whether this is a variable declaration or a type/function declaration.
     */
    public final boolean isVariableDeclaration;

    /**
     * whether this is type deceleration
     */
    public final boolean isTypeDeclaration;

    /**
     * There are three types of scope: Class/ClassScan, Function, Block
     * Class and Function scopes are considered enclosing scope, and have special rules for resolving.
     * <br /><br />
     * The rules for this field are the following:
     * <ul>
     * <li>For every block scope, increase value by 10.</li>
     * <li>For every enclosing scope, increase value by 1.</li>
     * </ul>
     *
     * @see types.TYPE_FOR_SCOPE_BOUNDARIES.Scope
     */
    final int scopeMajor;

    SymbolTableEntry(
            @NotNull Symbol symbol,
            boolean isVariableDeclaration,
            boolean isTypeDeclaration,
            int index,
            SymbolTableEntry next,
            SymbolTableEntry prev,
            int prevtop_index,
            int scopeMajor) {
        this.index = index;
        this.symbol = symbol;
        this.next = next;
        this.prev = prev;
        this.prevtop_index = prevtop_index;
        this.scopeMajor = scopeMajor;

        this.name = symbol.getName();
        this.isVariableDeclaration = isVariableDeclaration;
        this.isTypeDeclaration = isTypeDeclaration;
    }
}
