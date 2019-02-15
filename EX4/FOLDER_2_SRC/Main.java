import asm.Mips;
import ast.AST_PROGRAM;
import ir.analysis.IRBlock;
import ir.analysis.IRBlockGenerator;
import ir.analysis.liveness.LimitedRegisterAllocator;
import ir.commands.IRCommand;
import ir.commands.arithmetic.IRBinOpCommand;
import ir.commands.arithmetic.Operation;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRIfNotZeroCommand;
import ir.commands.flow.IRLabel;
import ir.commands.functions.IRCallCommand;
import ir.registers.Register;
import ir.registers.ReturnRegister;
import ir.registers.TempRegister;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.Graphwiz;
import utils.errors.SemanticException;

import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] argv) {
        String inputFilename = argv[0];
        String outputFilename = argv[1];
        try (FileReader fileReader = new FileReader(inputFilename);
             PrintWriter fileWriter = new PrintWriter(outputFilename)) {
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);
            try {
                // lex and parse the input
                AST_PROGRAM AST = (AST_PROGRAM) parser.parse().value;
                // semantic analysis
                AST.semant(SymbolTable.getInstance());
                // produce IR
                IRContext context = new IRContext();
                AST.irMe(context);
                // generate assembly
                Mips mips = new Mips();
                mips.process(context.getCommands(), context.getVirtualTables(), context.getConstantStrings(), context.getGlobals());
                String assembly = mips.export();
                // write result
                fileWriter.write(assembly);
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Invalid syntax
                fileWriter.write("ERROR(" + parser.line + ")\n");
                System.out.println("Syntax error ERROR(" + parser.line + ")");
            } catch (ClassCastException e) {
                e.printStackTrace();
                // Valid syntax but program doesn't start as AST_PROGRAM
                fileWriter.write("ERROR(1)\n");
                System.out.println("Syntax error starting rule");
            } catch (UnsupportedOperationException e) {
                e.printStackTrace();
                // Lexer throws error
                fileWriter.write("ERROR(" + lexer.getLine() + ")\n");
                System.out.println("lexer ERROR(" + lexer.getLine() + ")");
            } catch (SemanticException e) {
                e.printStackTrace();
                // semantic analysis throws error
                fileWriter.write("ERROR(" + e.getNode().lineNumber + ")\n");
                System.out.println("semantic ERROR(" + e.getNode().lineNumber + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


