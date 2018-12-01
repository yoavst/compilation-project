/***********/
/* PACKAGE */
/***********/
package symbols;

/*******************/
/* PROJECT IMPORTS */
/*******************/

import types.Type;

/**********************/
/* SYMBOL TABLE ENTRY */

/**********************/
public class SymbolTableEntry {
    /*********/
    /* index */
    /*********/
    int index;

    /********/
    /* name */
    /********/
    public String name;

    /******************/
    /* TYPE value ... */
    /******************/
    public Type type;

    /*********************************************/
    /* prevtop and next symbol table entries ... */
    /*********************************************/
    public SymbolTableEntry prevtop;
    public SymbolTableEntry next;

    /****************************************************/
    /* The prevtop_index is just for debug purposes ... */
    /****************************************************/
    public int prevtop_index;

    public boolean isVariableDeclaration;

    /******************/
    /* CONSTRUCTOR(S) */

    /******************/
    public SymbolTableEntry(
            String name,
            Type type,
            int index,
            SymbolTableEntry next,
            SymbolTableEntry prevtop,
            int prevtop_index,
            boolean isVariableDeclaration) {
        this.index = index;
        this.name = name;
        this.type = type;
        this.next = next;
        this.prevtop = prevtop;
        this.prevtop_index = prevtop_index;
        this.isVariableDeclaration = true;
    }
}
