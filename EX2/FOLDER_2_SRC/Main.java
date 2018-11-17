import ast.AST_PROGRAM;
import utils.Graphwiz;

import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

public class Main {
    static public void main(String argv[]) {
        String inputFilename = argv[0];
        String outputFilename = argv[1];
        try (FileReader fileReader = new FileReader(inputFilename);
             PrintWriter fileWriter = new PrintWriter(outputFilename)) {
            Lexer lexer = new Lexer(fileReader);
            Parser parser = new Parser(lexer);
            try {
                AST_PROGRAM AST = (AST_PROGRAM) parser.parse().value;
                fileWriter.write("OK\n");
                AST.printMe();
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
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


