package mini_python;

import mini_python.exception_handling.CompilationExceptionHandler;
import mini_python.syntax.File;
import mini_python.syntax.TFile;

public class Main {

	static boolean parse_only = false;
	static boolean type_only = false;
	static boolean debug = false;
	static String file = null;

	static void usage() {
		System.err.println("minipython [--parse-only] [--type-only] file.py");
		System.exit(1);
	}

	public static void main(String[] args) throws Exception {
		for (String arg : args)
			if (arg.equals("--parse-only"))
				parse_only = true;
			else if (arg.equals("--type-only"))
				type_only = true;
			else if (arg.equals("--debug")) {
				debug = true;
				Typing.debug = true;
				Compile.debug = true;
			} else {
				if (file != null)
					usage();
				if (!arg.endsWith(".py"))
					usage();
				file = arg;
			}
		if (file == null)
			file = "test.py";

		java.io.Reader reader = new java.io.FileReader(file);
		Lexer lexer = new MyLexer(reader);
		MyParser parser = new MyParser(lexer);
		try {
			File f = (File) parser.parse().value;
			if (parse_only)
				System.exit(0);
			TFile tf = Typing.file(f);
			if (type_only)
				System.exit(0);
			X86_64 asm = Compile.file(tf);
			String file_s = file.substring(0, file.length() - 3) + ".s";
			asm.printToFile(file_s);
		} catch (Exception e) {
			CompilationExceptionHandler handler = new CompilationExceptionHandler(file);
			handler.handle(e);
		} catch (Error e) {
			// là on est sur une erreur ?
			System.out.println("Error:");
			System.out.println(file + ":" + e.getMessage());
			System.exit(1);
		}
	}

}
