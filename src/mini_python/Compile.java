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
	 * <p>
	 * NOTE: because all arguments are evaluated and pushed to the stack, and then copied to local variables also
	 * allocated on the stack, we do not need to try and optimize the function call using registers.
	 * <p>
	 * As an optimization, we make the local variables of all functions (except main) refer to the stack positions before
	 * the new stack frame and return value (ie 16(%rbp), 24(%rbp), etc instead of -8(%rbp), -16(%rbp), etc) because
	 * we are essentially duplicating the stack frame for each function call.
	 */
	static void acceptFunc(Compiler compiler, TDef def) {
		compiler.x86_64.label("func_" + def.f.name); // Prepend "func_" in order to avoid collisions

		// Initialize the function stack frame
		compiler.x86_64.pushq(Regs.RBP);
		compiler.x86_64.movq(Regs.RSP, Regs.RBP);

		// We do not preallocate stack space, as we will just push values from the argument stack frame
		// to the local function parameter stack frame.
		
		// Move all arguments to the stack frame that we just allocated
		// int argCount = def.f.params.size();

		// Move all arguments to the new stack frame. The 1st argument is the last that was pushed to the stack
		// (so that depending on our optimization strategy, we have the choice to reuse the previous stack frame,
		// with arguments having their absolute stack frame offset always increasing in the order of arguments).
		// for (int i = 0; i < argCount; i++) {
			// WE DO THE OPTIMIZATION ! ˇˇˇˇˇˇˇˇˇˇˇˇ
			// NOTE: if we reused the previous stack frame, we would save <arg_count> instructions.
			// compiler.x86_64.pushq((i + 2) * 8 + "(%rbp)"); // Push the argument to the stack
		// }

		// Allocate stack space for the local variables of the function	
		// Force allocation of an even number of local variables to ensure stack alignment
		if (-def.f.localVariablesOffset % 16 != 0) {
			def.f.localVariablesOffset -= 8;
		}


		int additionalOffset = def.f.localVariablesOffset; // Do not add parameters, that were already added to the stack frame
		if (additionalOffset != 0) {
			compiler.x86_64.subq("$" + -additionalOffset, Regs.RSP);
		} 


		// Accept the function body. It contains return statements, we do not need to restore the stack frame
		// and dealloc local variables here, the return statements will handle it in our compiler.
		compiler.currentFunction = def; // Set the current function reference for cleaning up the correct stack size upon returning.
		def.body.accept(compiler);
	}
}
