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

		// Register internal functions
		internalFunctions(x86_64);

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

	/**
	 * Hook to register internal functions used for commodity
	 */
	static void internalFunctions(X86_64 x86_64) {

		// Define the print_bool function
		// This function receives the boolean value as the first argument in %rdi
		// It assumes that the stack is aligned to 16 bytes
		x86_64.label("_print_bool");

		// Compare the value to 0
		x86_64.cmpq("$0", Regs.RDI);
		x86_64.je("_print_bool_false"); // If it is 0, jump to the "false" label

		// Print True
		x86_64.movq("$true", Regs.RDI); // %rdi = format string for bools
		x86_64.jmp("_print_bool_end");

		// Print False
		x86_64.label("_print_bool_false");
		x86_64.movq("$false", Regs.RDI); // %rdi = format string for bools

		x86_64.label("_print_bool_end");

		// Call printf and newline
		x86_64.call("printf");
		x86_64.movq("$10", Regs.RDI); // 10 is the code for '\n'
		x86_64.call("putchar");

		x86_64.ret();
	}

}
