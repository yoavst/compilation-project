import ast.AST_PROGRAM;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.Graphwiz;
import utils.errors.SemanticException;

import java.io.FileReader;
import java.io.PrintWriter;

public class Main {
    public static void main(String[] argv) {
        String inputFilename = argv[0];
        String outputFilename = argv[1];
        try (FileReader fileReader = new FileReader(inputFilename);
             PrintWriter fileWriter = new PrintWriter(outputFilename)) {
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);
            try {
                AST_PROGRAM AST = (AST_PROGRAM) parser.parse().value;
                AST.semant(SymbolTable.getInstance());
                AST.printMe();
                fileWriter.write("OK\n");
                Graphwiz.getInstance().finalizeFile();

                IRContext context = new IRContext();
                AST.irMe(context);
                System.out.println(context.toString());

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


