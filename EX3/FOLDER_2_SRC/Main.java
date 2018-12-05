import ast.AST_PROGRAM;
import symbols.SymbolTable;
import utils.Graphwiz;
import utils.SemanticException;

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
                System.out.println("OK");
            } catch (IllegalStateException e) {
                // Invalid syntax
                fileWriter.write("ERROR(" + parser.line + ")\n");
                System.out.println("Syntax error");
            } catch (ClassCastException e) {
                // Valid syntax but program doesn't start as AST_PROGRAM
                fileWriter.write("ERROR(1)\n");
                System.out.println("Syntax error starting rule");
            } catch (UnsupportedOperationException e) {
                // Lexer throws error
                fileWriter.write("ERROR(" + lexer.getLine() + ")\n");
                System.out.println("lexer ERROR(" + lexer.getLine() + ")");
            } catch (SemanticException e) {
                // semantic analysis throws error
                fileWriter.write("ERROR(" + e.getNode().lineNumber + ")\n");
                System.out.println("semantic ERROR(" + e.getNode().lineNumber + ")");
                e.printStackTrace(); // FIXME
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


