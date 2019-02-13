package ast;

import ast.declarations.AST_DEC;
import ast.declarations.AST_DEC_VAR;
import ir.flow.IRLabel;
import ir.functions.IRCallCommand;
import ir.functions.IRPopCommand;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import ir.utils.IRContext;
import symbols.Symbol;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.List;

public class AST_PROGRAM extends AST_Node {
    @NotNull
    public AST_DEC[] declarations;
    public List<Symbol> globals;

    public AST_PROGRAM(@NotNull AST_DEC[] declarations) {
        this.declarations = declarations;
    }

    @NotNull
    @Override
    protected String name() {
        return "Program";
    }

    @Override
    public void printMe() {
        super.printMe();
        for (AST_DEC declaration : declarations) {
            printAndEdge(declaration);
        }
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        for (AST_DEC declaration : declarations) {
            declaration.semantHeader(symbolTable);
            declaration.semant(symbolTable);
        }
        globals = symbolTable.getGlobalSymbols();
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        context.openScope("global", globals, IRContext.ScopeType.Global, false, false);
        for (AST_DEC declaration : declarations) {
                declaration.irMe(context);
        }

        // generate main
        IRLabel label = IRContext.STDLIB_FUNCTION_MAIN;
        context.label(label);
        Register temp = context.newRegister();
        for (IRLabel preMainFunction : context.getPreMainFunctions()) {
            context.command(new IRCallCommand(preMainFunction));
            context.command(new IRPopCommand(temp));
        }

        context.command(new IRCallCommand(context.functionLabelFor(IRContext.MAIN_SYMBOL)));
        context.command(new IRPopCommand(temp));
        context.exit();

        // not closing the scope so global variables will be saved
        return NonExistsRegister.instance;
    }
}
