package mini_python;

class Compile {

	static boolean debug = true;

	static X86_64 file(TFile f) {
		Compiler.debug = debug;
		Compiler compiler = new Compiler();
		compiler.x86_64.globl("main");
		compiler.x86_64.label("main");
		f.l.getFirst().body.accept(compiler);
		compiler.x86_64.xorq("%rax", "%rax");
		compiler.x86_64.ret();

		return compiler.x86_64;
	}

}
