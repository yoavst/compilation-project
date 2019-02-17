package symbols;

import types.TYPE_FOR_SCOPE_BOUNDARIES;
import types.TYPE_FOR_SCOPE_BOUNDARIES.Scope;
import types.Type;
import types.TypeClass;
import types.TypeFunction;
import types.builtins.TypeArray;
import types.builtins.TypeInt;
import types.builtins.TypeString;
import types.builtins.TypeVoid;
import utils.Flags;
import utils.NotNull;
import utils.Nullable;
import utils.Utils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.function.Predicate;

public class SymbolTable {
    private int hashArraySize = 13;
    private SymbolTableEntry[] table = new SymbolTableEntry[hashArraySize];
    private SymbolTableEntry top;
    private Stack<Scope> scopeStack = new Stack<>();
    private int topIndex = 0;
    private TypeClass enclosingClass;
    private Symbol enclosingFunction;
    private int currentScopeMajor = 0;
    private List<TypeClass> registeredClasses = new ArrayList<>();
    private List<TypeArray> registeredArrays = new ArrayList<>();
    private List<Symbol> globalSymbols = new ArrayList<>();

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
        enter(name, t, false, false);
    }

    public void enter(String name, Type t, boolean isVariableDeclaration) {
        enter(name, t, isVariableDeclaration, false);
    }

    public void enter(String name, Type t, boolean isVariableDeclaration, boolean isAddingForNamespace) {
        enter(name, t, isVariableDeclaration, isAddingForNamespace, false);
    }

    public void enter(String name, Type t, boolean isVariableDeclaration, boolean isAddingForNamespace, boolean isTypeDeclaration) {
        final Symbol s;
        if (enclosingFunction == null)
            s = new Symbol(name, t, enclosingClass);
        else
            s = new Symbol(name, t);

        int hashValue = hash(name);
        SymbolTableEntry next = table[hashValue];
        SymbolTableEntry e = new SymbolTableEntry(s, isVariableDeclaration, isTypeDeclaration , hashValue, next, top, topIndex++, currentScopeMajor);
        top = e;
        table[hashValue] = e;

        if (currentScopeMajor == 0 && !isAddingForNamespace && !isTypeDeclaration) {
            globalSymbols.add(s);
        }

        if (t.isArray() && !isVariableDeclaration && isAddingForNamespace)
            registeredArrays.add((TypeArray) t);

        PrintMe();
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found
     */
    @Nullable
    public Symbol find(@NotNull String name) {
        return find(name, null);
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found
     *
     * @param filter Applying filter on results, returns element only if accepted by the filter.
     */
    @Nullable
    private Symbol find(@NotNull String name, @Nullable Predicate<SymbolTableEntry> filter) {
        return find(name, filter, null);
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found
     *
     * @param filter             Applying filter on results, returns element only if accepted by the filter.
     * @param shouldContinueLoop Continuing the loop until this return false or reached null.
     */
    @Nullable
    private Symbol find(@NotNull String name, @Nullable Predicate<SymbolTableEntry> filter, Predicate<SymbolTableEntry> shouldContinueLoop) {
        for (SymbolTableEntry e = table[hash(name)]; e != null && (shouldContinueLoop == null || shouldContinueLoop.test(e)); e = e.next) {
            if (name.equals(e.name) && (filter == null || filter.test(e))) {
                return e.symbol;
            }
        }

        return null;
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found or if went outside an enclosing scope.
     */
    public Symbol findInCurrentEnclosingScope(@NotNull String name) {
        int currentEnclosingScope = currentScopeMajor % 10;
        return find(name, null, e -> e.scopeMajor % 10 >= currentEnclosingScope);
    }

    /**
     * Find the inner-most scope element with given name, returning null if not found or if went outside of the current scope.
     */
    public Symbol findInCurrentScope(@NotNull String name) {
        return find(name, null, e -> e.scopeMajor == currentScopeMajor);
    }

    /**
     * Find the inner-most scope element with name that is a function, returning null if not found.
     *
     * @param startSearchingOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public Symbol findMethod(@NotNull String name, boolean startSearchingOutsideClass) {
        if (!startSearchingOutsideClass && enclosingClass != null) {
            Symbol symbol = enclosingClass.queryMethodRecursively(name);
            if (symbol != null)
                return symbol;
        }

        return find(name, entry -> !entry.isVariableDeclaration && entry.symbol.type.isFunction());
    }

    /**
     * Find the inner-most scope element with name that is a field, returning null if not found.
     *
     * @param startSearchingOutsideClass Whether or not to skip a method that is defined inside class scope
     */
    @Nullable
    public Symbol findField(@NotNull String name, boolean startSearchingOutsideClass) {
        if (enclosingFunction != null) {
            Symbol symbol = findInCurrentEnclosingScope(name);
            if (symbol != null)
                return symbol;
        }

        if (!startSearchingOutsideClass && enclosingClass != null) {
            Symbol symbol = enclosingClass.queryFieldRecursively(name);
            if (symbol != null)
                return symbol;
        }

        return find(name, entry -> entry.isVariableDeclaration);
    }

    /**
     * Find a class with the given name, returning null if not found.
     */
    @Nullable
    public TypeClass findClassType(@NotNull String name) {
        Symbol s = find(name, entry -> !entry.isVariableDeclaration && entry.symbol.type.isClass());
        return s != null ? (TypeClass) s.type : null;
    }

    /**
     * Find an array with the given name, returning null if not found.
     */
    @Nullable
    public TypeArray findArrayType(@NotNull String name) {
        Symbol s = find(name, entry -> !entry.isVariableDeclaration && entry.symbol.type.isArray());
        return s != null ? (TypeArray) s.type : null;
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
     * Return the enclosing class for the current state of the symbol table, returning null if no such class.
     */
    @Nullable
    public TypeClass getEnclosingClass() {
        return enclosingClass;
    }

    /**
     * Return the enclosing function for the current state of the symbol table, returning null if no such function.
     */
    @Nullable
    public Symbol getEnclosingFunction() {
        return enclosingFunction;
    }

    /**
     * Return all the classes that were ever loaded to the symbol table
     */
    @NotNull
    public List<TypeClass> getClasses() {
        return registeredClasses;
    }

    /**
     * Return all the array types that were ever loaded to the symbol table
     */
    @NotNull
    public List<TypeArray> getArrays() {
        return registeredArrays;
    }

    /**
     * Return all the global symbols
     */
    @NotNull
    public List<Symbol> getGlobalSymbols() {
        return globalSymbols;
    }

    /**
     * Begins a new scope by adding <SCOPE-BOUNDARY> to the Hashmap.
     * In addition, if it is a class or function scope, update the enclosing field.
     */
    public void beginScope(@NotNull Scope scope, @Nullable TypeClass enclosingType, @Nullable Symbol enclosingSymbol, String debugInfo) {
        if (scope == Scope.Block) {
            currentScopeMajor += 10;
        } else {
            currentScopeMajor += 1;
        }
        scopeStack.push(scope);
        enter("SCOPE-BOUNDARY", new TYPE_FOR_SCOPE_BOUNDARIES("[" + currentScopeMajor + "] " + debugInfo, scope));

        if (scope == Scope.Function) {
            enclosingFunction = enclosingSymbol;
        } else if (scope == Scope.Class) {
            enclosingClass = (TypeClass) enclosingType;
            registeredClasses.add(enclosingClass);
        } else if (scope == Scope.ClassScan) {
            enclosingClass = (TypeClass) enclosingType;
        }

        PrintMe();
    }

    /**
     * Ending the current scope by popping all the symbols in the current scope.
     * If the current scope with {@link Scope#ClassScan} it will also register all the fields and method to the class.
     */
    public List<Symbol> endScope() {
        List<Symbol> symbols = new ArrayList<>();
        // we create a scope to semant the function parameters in the header
        boolean shouldSaveToClass = scopeStack.peek() == Scope.ClassScan && getEnclosingFunction() == null;
        // Pop elements from the symbol table stack until a SCOPE-BOUNDARY is hit
        while (!top.name.equals("SCOPE-BOUNDARY")) {
            if (shouldSaveToClass) {
                // register the method/field into the class type.
                Type declaration = top.symbol.type;
                if (declaration.isFunction()) {
                    enclosingClass.registerMethod(top.name, (TypeFunction) declaration);
                } else {
                    enclosingClass.registerField(top.name, declaration);
                }
            }

            if (top.isVariableDeclaration)
                symbols.add(top.symbol);
            table[top.index] = top.next;
            topIndex = topIndex - 1;
            top = top.prev;
        }

        Scope scope = ((TYPE_FOR_SCOPE_BOUNDARIES) top.symbol.type).scope;
        if (scope == Scope.Function) {
            enclosingFunction = null;
        } else if (scope == Scope.Class || scope == Scope.ClassScan) {
            enclosingClass = null;
        }

        // remove the boundary
        table[top.index] = top.next;
        topIndex = topIndex - 1;
        top = top.prev;

        // update current scope
        currentScopeMajor = top == null ? 0 : top.scopeMajor;
        scopeStack.pop();

        PrintMe();
        return symbols;
    }

    public static int n = 0;

    public void PrintMe() {
        if (Flags.VERBOSE) {
            int i = 0;
            int j = 0;
            String dirname = "./FOLDER_5_OUTPUT/";
            String filename = String.format("SYMBOL_TABLE_%d_IN_GRAPHVIZ_DOT_FORMAT.txt", n++);

            try {
            /*
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
                                it.symbol.type.name,
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
    }

    private static SymbolTable instance = null;

    protected SymbolTable() {
    }


    public static SymbolTable getInstance() {
        if (instance == null) {
            instance = new SymbolTable();

            instance.enter("int", TypeInt.instance, false, true, true);
            instance.enter("string", TypeString.instance, false, true, true);
            instance.enter(
                    "PrintInt",
                    new TypeFunction(
                            "PrintInt",
                            TypeVoid.instance,
                            Utils.mutableListOf(TypeInt.instance)));
            instance.enter(
                    "PrintString",
                    new TypeFunction(
                            "PrintString",
                            TypeVoid.instance,
                            Utils.mutableListOf(TypeString.instance)));
            instance.enter(
                    "PrintTrace",
                    new TypeFunction(
                            "PrintTrace",
                            TypeVoid.instance
                    ));
        }
        return instance;
    }
}
