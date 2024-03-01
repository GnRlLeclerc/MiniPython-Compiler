package mini_python;

import mini_python.registers.Registers;
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
		int localOffset = f.l.getLast().f.localVariablesOffset;
		if (localOffset != 0) {
			compiler.x86_64.subq("$" + -localOffset, Regs.RSP);
		}

		// Get the main function and accept it
		if (debug) {
			System.out.println("\nCompiling main function");
			System.out.println("------------------------");
		}
		TDef main = f.l.removeLast();
		main.body.accept(compiler);

		// Exit instructions
		exit(compiler.x86_64);

		// Compile the other functions after the main function exit point
		for (TDef def : f.l) {
			if (debug) {
				System.out.println("\nCompiling function " + def.f.name + " as func_" + def.f.name);
				System.out.println("--------------------");
			}
			acceptFunc(compiler, def);
		}

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

	/**
	 * Exit point of the main function
	 */
	static void exit(X86_64 x86_64) {
		x86_64.xorq(Regs.RAX, Regs.RAX);
		x86_64.movq(0, Regs.RDI); // Return code as argument
		x86_64.call("exit");
	}

	/**
	 * Accept a function definition (not the main function)
	 */
	static void acceptFunc(Compiler compiler, TDef def) {
		compiler.x86_64.label("func_" + def.f.name); // Prepend "func_" in order to avoid collisions

		// Initialize the function stack frame
		compiler.x86_64.pushq(Regs.RBP);
		compiler.x86_64.movq(Regs.RSP, Regs.RBP);

		// Allocate space for the local variables of the function on the stack
		compiler.x86_64.subq("$" + -def.f.localVariablesOffset, Regs.RSP);

		// Move all arguments to the stack frame that we just allocated
		int argCount = def.f.params.size();

		// Move the first 6 arguments to the stack frame
		for (int i = 0; i < Math.min(6, argCount); i++) {
			compiler.x86_64.movq(Registers.argReg(i).getCode(), -(i + 1) * 8 + "(%rbp)");
		}

		// Move the rest of the arguments to the stack frame
		for (int i = 6; i < argCount; i++) {
			compiler.x86_64.movq((i - 4) * 8 + "(%rbp)", -(1 + i) * 8 + "(%rbp)");
		}

		// Accept the function body. It contains return statements, we do not need to restore the stack frame
		// and dealloc local variables.
		compiler.currentFunction = def; // Set the current function reference for cleaning up the correct stack size upon returning.
		compiler.stackAlignOffset = 0; // Reset stack alignment offset.
		// We align the stack to 16 bytes before calling our functions,
		// else it is impossible to keep track of its changes when multiple calls occur.
		def.body.accept(compiler);
	}
}
