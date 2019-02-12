package ast.statements;

import ast.expressions.AST_EXP;
import ir.IRContext;
import ir.flow.IRGotoCommand;
import ir.flow.IRIfZeroCommand;
import ir.flow.IRLabel;
import ir.registers.NonExistsRegister;
import ir.registers.Register;
import symbols.Symbol;
import symbols.SymbolTable;
import types.TypeError;
import types.builtins.TypeInt;
import utils.NotNull;
import utils.errors.SemanticException;

import java.util.ArrayList;
import java.util.List;

import static types.TYPE_FOR_SCOPE_BOUNDARIES.Scope.Block;

public class AST_STMT_WHILE extends AST_STMT {
    @NotNull
    public AST_EXP cond;
    @NotNull
    public AST_STMT[] body;
    private Symbol enclosingFunction;


    public AST_STMT_WHILE(@NotNull AST_EXP cond, @NotNull AST_STMT[] body) {
        this.cond = cond;
        this.body = body;
    }

    @NotNull
    @Override
    protected String name() {
        return "WHILE(...)";
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
            throwSemantic("while condition can only be int, received: " + cond.getType());
        }

        symbolTable.beginScope(Block, null, null,"while(...)");
        for (AST_STMT statement : body) {
            statement.semant(symbolTable);
        }
        symbolTable.endScope();

        enclosingFunction = symbolTable.getEnclosingFunction();
    }

    @Override
    public @NotNull Register irMe(IRContext context) {
        IRLabel afterLabel = context.generateLabel(enclosingFunction.name + "_after_while");
        IRLabel conditionLabel = context.generateLabel(enclosingFunction.name + "_while_condition");

        context.putLabel(conditionLabel);
        Register conditionRegister = cond.irMe(context);
        context.freeRegister(conditionRegister);

        // if not true then jump, otherwise continue
        context.addCommand(new IRIfZeroCommand(conditionRegister, afterLabel));
        // insert body
        List<Register> temps = new ArrayList<>(body.length);
        for (AST_STMT statement : body) {
            Register temp = statement.irMe(context);
            temps.add(temp);
        }
        context.addCommand(new IRGotoCommand(conditionLabel));
        context.putLabel(afterLabel);

        context.freeRegister(conditionRegister);
        temps.forEach(context::freeRegister);
        return NonExistsRegister.instance;
    }
}