
import java.io.*;
import java.io.PrintWriter;

import java_cup.runtime.Symbol;

public class Main {
	private static final boolean DEBUG = true;

	static public void main(String argv[]) {
		String inputFilename = argv[0];
		String outputFilename = argv[1];

		try (FileReader fileReader = new FileReader(inputFilename);
				PrintWriter fileWriter = new PrintWriter(outputFilename)) {

			Lexer l = new Lexer(fileReader);
			Symbol s;

			while ((s = l.next_token()).sym != TokenNames.EOF) {
				printToken(s, l, fileWriter);
			}

			l.yyclose();
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			
			try(PrintWriter newFileWriter = new PrintWriter(outputFilename)) {
				newFileWriter.write("ERROR");
			} catch(IOException ignored) {
				e.printStackTrace();
			}

		} catch(IOException e) {
			e.printStackTrace();
		}
	}

	private static void print(PrintWriter writer, Object data) {
		if (DEBUG) {
			System.out.print(data);
		}

		writer.print(data.toString());
	}

	private static void printToken(Symbol symbol, Lexer lexer, PrintWriter writer) {
		print(writer, TokenNames.NAMES[symbol.sym]);
		if (symbol.sym == TokenNames.STRING) {
			print(writer, "(\"");
			print(writer, symbol.value);
			print(writer, "\")");
		} else if (symbol.sym == TokenNames.ID || symbol.sym == TokenNames.INT) {
			print(writer, "(");
			print(writer, symbol.value);
			print(writer, ")");
		}
		print(writer, "[");
		print(writer, lexer.getLine());
		print(writer, ",");
		print(writer, lexer.getTokenStartPosition());
		print(writer, "]");
		print(writer, "\n");
	}
}
