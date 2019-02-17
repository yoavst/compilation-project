import asm.Mips;
import ast.AST_PROGRAM;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.Utils;
import utils.errors.SemanticException;

import java.io.FileReader;
import java.io.PrintWriter;

import static utils.Utils.tick;

public class Main {
    public static void main(String[] args) {
        String inputFilename = args[0];
        String outputFilename = args[1];
        String irOutputFilename = args.length >= 3 ? args[2] : args[1] + ".ir";

        try (FileReader fileReader = new FileReader(inputFilename);
             PrintWriter irFileWriter = new PrintWriter(irOutputFilename);
             PrintWriter fileWriter = new PrintWriter(outputFilename)) {
            tick("Compiler has started.");
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);
            try {
                // lex and parse the input
                AST_PROGRAM AST = (AST_PROGRAM) parser.parse().value;
                tick("Lexing and Parsing");

                // semantic analysis
                AST.semant(SymbolTable.getInstance());
                tick("Semantic analysis");

                // produce IR
                IRContext context = new IRContext();
                AST.irMe(context);
                irFileWriter.write(context.toString());
                tick("IR Generation");

                // generate assembly
                Mips mips = new Mips();
                mips.process(context.getCommands(), context.getVirtualTables(), context.getConstantStrings(), context.getGlobals());
                String assembly = mips.export();
                tick("Assembly Generation");
                // write result
                fileWriter.write(assembly);
                tick("[Done] Output file was updated.");

            } catch (IllegalStateException e) {
                e.printStackTrace();
                // Invalid syntax
                fileWriter.write("ERROR(" + parser.line + ")\n");
                tick("[Error] Syntax error ERROR(" + parser.line + ")");

            } catch (ClassCastException e) {
                e.printStackTrace();
                // Valid syntax but program doesn't start as AST_PROGRAM
                fileWriter.write("ERROR(1)\n");
                tick("[Error] Syntax error starting rule");
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
                tick("[Error] semantic ERROR(" + e.getNode().lineNumber + ")");
            }
        } catch (Exception e) {
            e.printStackTrace();
            tick("[Error] " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }
}


