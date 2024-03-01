package mini_python;

import mini_python.registers.Regs;

class Compile {

	static boolean debug = true;

	static X86_64 file(TFile f) {
		Compiler.debug = debug;
		Compiler compiler = new Compiler();


		// Begin instructions
		begin(compiler.x86_64);

		// Allocate space for the local variables of the main function
		// Because the main function is not called via TEcall, its local stack variables will not be allocated by our Compiler class
		compiler.x86_64.subq("$" + -f.l.getFirst().f.localVariablesOffset, Regs.RSP);

		// Compile the first function. The first function is the main one
		// TODO: we will need to accept all other functions as well !
		f.l.getFirst().body.accept(compiler);

		// Exit instructions
		exit(compiler.x86_64);

		return compiler.x86_64;
	}

	static void begin(X86_64 x86_64) {
		x86_64.globl("main");
		x86_64.label("main");

		// First instruction: push the current stack pointer position to %rbp
		// This basically initializes the stack frame
		x86_64.pushq(Regs.RBP); // Save the initial stack frame (although it is not used)
		x86_64.movq(Regs.RSP, Regs.RBP);
	}

	static void exit(X86_64 x86_64) {
		x86_64.xorq(Regs.RAX, Regs.RAX);
		x86_64.movq(0, Regs.RDI); // Return code as argument
		x86_64.call("exit");
	}
}
