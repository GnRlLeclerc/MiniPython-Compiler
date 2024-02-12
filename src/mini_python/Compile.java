package mini_python;

class Compile {

	static boolean debug = true;

	static X86_64 file(TFile f) {
		Compiler.debug = debug;
		Compiler compiler = new Compiler();
		f.l.getFirst().body.accept(compiler);

		return compiler.x86_64;
	}

}
