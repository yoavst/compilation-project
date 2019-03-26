package ast.statements;

import ast.expressions.AST_EXP;
import ir.utils.IRContext;
import ir.commands.flow.IRIfZeroCommand;
import ir.commands.flow.IRLabel;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import symbols.Symbol;
import symbols.SymbolTable;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.List;

import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Block;

public class AST_STMT_IF extends AST_STMT {
    @NotNull
    public AST_EXP cond;
    @NotNull
    public AST_STMT[] body;
    private Symbol enclosingFunction;
    private List<Symbol> locals;

    public AST_STMT_IF(@NotNull AST_EXP cond, @NotNull AST_STMT[] body) {
        this.cond = cond;
        this.body = body;
    }

    @NotNull
    @Override
    protected String name() {
        return "IF";
    }

    @Override
    public void printMe() {
        super.printMe();
        printAndEdge(cond);
        addListUnderWrapper("body", body);
    }

    @Override
    protected void semantMe(SymbolTable symbolTable) throws SemanticException {
        cond.semant(symbolTable);
        if (!cond.getType().canBeCastedToBoolean()) {
            throwSemantic("if condition can only be int, received: " + cond.getType());
        }

        symbolTable.beginScope(Block, null, null,"if(...)");
        for (AST_STMT statement : body) {
            statement.semant(symbolTable);
        }
        locals = symbolTable.endScope();

        enclosingFunction = symbolTable.getEnclosingFunction();
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        Register conditionRegister = cond.irMe(context);
        IRLabel afterLabel = context.newLabel("after_if");
        // if not true then jump, otherwise continue
        context.command(new IRIfZeroCommand(conditionRegister, afterLabel));
        // insert body
        context.openScope("if_body", locals, IRContext.ScopeType.Inner, false, false);
        for (AST_STMT statement : body) {
            statement.irMe(context);
        }
        context.closeScope();

        context.label(afterLabel);
        return NonExistsRegister.instance;
    }
}