/***********/
/* PACKAGE */
/***********/
package symbols;

/*******************/
/* GENERAL IMPORTS */
/*******************/

import types.TYPE_FOR_SCOPE_BOUNDARIES;
import types.TYPE_FOR_SCOPE_BOUNDARIES.Scope;
import types.Type;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import types.builtins.TypeVoid;
import utils.NotNull;
import utils.Nullable;
import utils.Utils;

import java.io.PrintWriter;

/*******************/
/* PROJECT IMPORTS */
/*******************/

/****************/
/* SYMBOL TABLE */

/****************/
public class SymbolTable {
    private int hashArraySize = 13;

    /**********************************************/
    /* The actual symbol table data structure ... */
    /**********************************************/
    private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
    private SymbolTableEntry top;
    private int top_index = 0;

    /**************************************************************/
    /* A very primitive hash function for exposition purposes ... */

    /**************************************************************/
    private int hash(String s) {
        if (s.charAt(0) == 'l') {
            return 1;
        }
        if (s.charAt(0) == 'm') {
            return 1;
        }
        if (s.charAt(0) == 'r') {
            return 3;
        }
        if (s.charAt(0) == 'i') {
            return 6;
        }
        if (s.charAt(0) == 'd') {
            return 6;
        }
        if (s.charAt(0) == 'k') {
            return 6;
        }
        if (s.charAt(0) == 'f') {
            return 6;
        }
        if (s.charAt(0) == 'S') {
            return 6;
        }
        return 12;
    }

    public void enter(String name, Type t) {
        enter(name, t, false);
    }

    /****************************************************************************/
    /* Enter a variable, function, class type or array type to the symbol table */

    /****************************************************************************/
    public void enter(String name, Type t, boolean isVariableDeclaration) {
        /*************************************************/
        /* [1] Compute the hash value for this new entry */
        /*************************************************/
        int hashValue = hash(name);

        /******************************************************************************/
        /* [2] Extract what will eventually be the next entry in the hashed position  */
        /*     NOTE: this entry can very well be null, but the behaviour is identical */
        /******************************************************************************/
        SymbolTableEntry next = table[hashValue];

        /**************************************************************************/
        /* [3] Prepare a new symbol table entry with name, type, next and prevtop */
        /**************************************************************************/
        SymbolTableEntry e = new SymbolTableEntry(name, t, hashValue, next, top, top_index++, isVariableDeclaration);

        /**********************************************/
        /* [4] Update the top of the symbol table ... */
        /**********************************************/
        top = e;

        /****************************************/
        /* [5] Enter the new entry to the table */
        /****************************************/
        table[hashValue] = e;

        /**************************/
        /* [6] Print Symbol Table */
        /**************************/
        PrintMe();
    }

    /***********************************************/
    /* Find the inner-most scope element with name */

    /***********************************************/
    @Nullable
    public Type find(@NotNull String name) {
        SymbolTableEntry e;

        for (e = table[hash(name)]; e != null; e = e.next) {
            if (name.equals(e.name)) {
                return e.type;
            }
        }

        return null;
    }

    /**
     * Find the inner-most scope element with name that is a function
     * @param searchOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public TypeFunction findMethod(@NotNull String name, boolean searchOutsideClass) {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }

    /**
     * Find a class with the given name
     */
    @Nullable
    public TypeClass findClassType(@NotNull String name) {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }

    /**
     * Find an array with the given name
     */
    @Nullable
    public TypeArray findArrayType(@NotNull String name) {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }


    /**
     * Find generalized type with the given name:
     * - string, int, void
     * - array type
     * - class type
     */
    @Nullable
    public Type findGeneralizedType(@NotNull String name) {
        if (name.equals(TypeInt.instance.name))
            return TypeInt.instance;
        else if (name.equals(TypeString.instance.name))
            return TypeString.instance;
        if (name.equals(TypeVoid.instance.name))
            return TypeVoid.instance;

        Type arrayType = findArrayType(name);
        if (arrayType != null)
            return arrayType;

        return findClassType(name);
    }

    /**
     * Find the inner-most scope element with name that is a field
     * @param searchOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public TypeArray findField(@NotNull String name, boolean searchOutsideClass) {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }


    /**
     * Return the enclosing class for the current state of the symbol table, returning null if no such class.
     */
    @Nullable
    public TypeClass getEnclosingClass() {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }

    /**
     * Return the enclosing function for the current state of the symbol table, returning null if no such function.
     */
    @Nullable
    public TypeFunction getEnclosingFunction() {
        //FIXME
        throw new IllegalStateException("Not implemented yet");
    }

    /***************************************************************************/
    /* begine scope = Enter the <SCOPE-BOUNDARY> element to the data structure */

    /***************************************************************************/
    public void beginScope(Scope scope) {
        /************************************************************************/
        /* Though <SCOPE-BOUNDARY> entries are present inside the symbol table, */
        /* they are not really types. In order to be able to debug print them,  */
        /* a special TYPE_FOR_SCOPE_BOUNDARIES was developed for them. This     */
        /* class only contain their type name which is the bottom sign: _|_     */
        /************************************************************************/
        enter(
                "SCOPE-BOUNDARY",
                new TYPE_FOR_SCOPE_BOUNDARIES("NONE", scope));

        /*********************************************/
        /* Print the symbol table after every change */
        /*********************************************/
        PrintMe();
    }

    /********************************************************************************/
    /* end scope = Keep popping elements out of the data structure,                 */
    /* from most recent element entered, until a <NEW-SCOPE> element is encountered */

    /********************************************************************************/
    public void endScope() {
        // FIXME handle scope type `ClassScan` and insert all methods and fields into class
        /**************************************************************************/
        /* Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit */
        /**************************************************************************/
        while (top.name != "SCOPE-BOUNDARY") {
            table[top.index] = top.next;
            top_index = top_index - 1;
            top = top.prevtop;
        }
        /**************************************/
        /* Pop the SCOPE-BOUNDARY sign itself */
        /**************************************/
        table[top.index] = top.next;
        top_index = top_index - 1;
        top = top.prevtop;

        /*********************************************/
        /* Print the symbol table after every change */
        /*********************************************/
        PrintMe();
    }

    public static int n = 0;

    public void PrintMe() {
        int i = 0;
        int j = 0;
        String dirname = "./FOLDER_5_OUTPUT/";
        String filename = String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt", n++);

        try {
            /*******************************************/
            /* [1] Open Graphviz text file for writing */
            /*******************************************/
            PrintWriter fileWriter = new PrintWriter(dirname + filename);

            /*********************************/
            /* [2] Write Graphviz dot prolog */
            /*********************************/
            fileWriter.print("digraph structs {\n");
            fileWriter.print("rankdir = LR\n");
            fileWriter.print("node [shape=record];\n");

            /*******************************/
            /* [3] Write Hash Table Itself */
            /*******************************/
            fileWriter.print("hashTable [label=\"");
            for (i = 0; i < hashArraySize - 1; i++) {
                fileWriter.format("<f%d>\n%d\n|", i, i);
            }
            fileWriter.format("<f%d>\n%d\n\"];\n", hashArraySize - 1, hashArraySize - 1);

            /****************************************************************************/
            /* [4] Loop over hash table array and print all linked lists per array cell */
            /****************************************************************************/
            for (i = 0; i < hashArraySize; i++) {
                if (table[i] != null) {
                    /*****************************************************/
                    /* [4a] Print hash table array[i] -> entry(i,0) edge */
                    /*****************************************************/
                    fileWriter.format("hashTable:f%d -> node_%d_0:f0;\n", i, i);
                }
                j = 0;
                for (SymbolTableEntry it = table[i]; it != null; it = it.next) {
                    /*******************************/
                    /* [4b] Print entry(i,it) node */
                    /*******************************/
                    fileWriter.format("node_%d_%d ", i, j);
                    fileWriter.format("[label=\"<f0>%s|<f1>%s|<f2>prevtop=%d|<f3>next\"];\n",
                            it.name,
                            it.type.name,
                            it.prevtop_index);

                    if (it.next != null) {
                        /***************************************************/
                        /* [4c] Print entry(i,it) -> entry(i,it.next) edge */
                        /***************************************************/
                        fileWriter.format(
                                "node_%d_%d -> node_%d_%d [style=invis,weight=10];\n",
                                i, j, i, j + 1);
                        fileWriter.format(
                                "node_%d_%d:f3 -> node_%d_%d:f0;\n",
                                i, j, i, j + 1);
                    }
                    j++;
                }
            }
            fileWriter.print("}\n");
            fileWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**************************************/
    /* USUAL SINGLETON IMPLEMENTATION ... */
    /**************************************/
    private static SymbolTable instance = null;

    /*****************************/
    /* PREVENT INSTANTIATION ... */

    /*****************************/
    protected SymbolTable() {
    }

    /******************************/
    /* GET SINGLETON INSTANCE ... */

    /******************************/
    public static SymbolTable getInstance() {
        if (instance == null) {
            /*******************************/
            /* [0] The instance itself ... */
            /*******************************/
            instance = new SymbolTable();

            /*****************************************/
            /* [1] Enter primitive types int, string */
            /*****************************************/
            instance.enter("int", TypeInt.instance);
            instance.enter("string", TypeString.instance);

            /*************************************/
            /* [2] How should we handle void ??? */
            /*************************************/

            /***************************************/
            /* [3] Enter library function PrintInt */
            /***************************************/
            instance.enter(
                    "PrintInt",
                    new TypeFunction(
                            "PrintInt",
                            TypeVoid.instance,
                            Utils.mutableListOf(TypeInt.instance)));

        }
        return instance;
    }
}
