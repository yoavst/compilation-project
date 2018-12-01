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
                fileWriter.write("OK\n");
                Graphwiz.getInstance().finalizeFile();
            } catch (IllegalStateException e) {
                // Invalid syntax
                fileWriter.write("ERROR(" + parser.line + ")\n");
            } catch (ClassCastException e) {
                // Valid syntax but program doesn't start as AST_PROGRAM
                fileWriter.write("ERROR(1)\n");
            } catch (UnsupportedOperationException e) {
                // Lexer throws error
                fileWriter.write("ERROR(" + lexer.getLine() + ")\n");
            } catch (SemanticException e) {
                // semantic analysis throws error
                fileWriter.write("ERROR(" + e.getNode().lineNumber + ")\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


