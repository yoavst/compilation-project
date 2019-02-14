import ast.AST_PROGRAM;
import ir.optimizations.IRBlock;
import ir.optimizations.LivenessAnalysis;
import ir.utils.IRContext;
import symbols.SymbolTable;
import utils.Graphwiz;
import utils.errors.SemanticException;

import java.io.FileReader;
import java.io.PrintWriter;
import java.util.List;

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
                List<IRBlock> blocks = context.getBlocks();

//                Register a = ReturnRegister.instance;
//                Register b = new TempRegister(2);
//                Register c = new TempRegister(3);
//                Register d = new TempRegister(4);
//
//                List<IRCommand> commands = Arrays.asList(
//                        new IRLabel("entry"),
//                        new IRBinOpCommand(b, c, Operation.Plus, d),
//                        new IRBinOpCommand(c, c, Operation.Plus, d),
//                        new IRIfNotZeroCommand(a, new IRLabel("if1")),
//                        new IRBinOpCommand(c, a, Operation.Plus, b),
//                        new IRGotoCommand(new IRLabel("after_if")),
//                        new IRLabel("if1"),
//                        new IRBinOpCommand(a, b, Operation.Plus, c),
//                        new IRBinOpCommand(d, a, Operation.Plus, c),
//                        new IRLabel("after_if"),
//                        new IRBinOpCommand(a, a, Operation.Plus, b),
//                        new IRBinOpCommand(d, b, Operation.Plus, c),
//                        new IRIfNotZeroCommand(a, new IRLabel("entry")),
//                        new IRCallCommand(new IRLabel("exit"))
//                        );
//                IRBlockGenerator generator = new IRBlockGenerator();
//                commands.forEach(generator::handle);
//                List<IRBlock> generatedBlocks = generator.finish();
//                LivenessAnalysis analysis = new LivenessAnalysis();
//                analysis.run(generatedBlocks);

                LivenessAnalysis analysis = new LivenessAnalysis();
                analysis.run(blocks);
                System.out.println();


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


