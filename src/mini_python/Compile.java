package mini_python;

import mini_python.registers.Regs;

class Compile {

	static boolean debug = true;

	static X86_64 file(TFile f) {
		Compiler.debug = debug;
		Compiler compiler = new Compiler();

		// Begin instructions
		begin(compiler.x86_64);

		f.l.getFirst().body.accept(compiler);

		// Exit instructions
		exit(compiler.x86_64);

		return compiler.x86_64;
	}

	static void begin(X86_64 x86_64) {
		x86_64.globl("main");
		x86_64.label("main");
	}

	static void exit(X86_64 x86_64) {
		x86_64.xorq(Regs.RAX, Regs.RAX);
		x86_64.movq(0, Regs.RDI); // Return code as argument
		x86_64.call("exit");
	}

}
