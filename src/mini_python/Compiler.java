package mini_python;

import mini_python.registers.Regs;

class Compiler implements TVisitor {

	static boolean debug = false;

	X86_64 x86_64;
	int cstId = 0;

	Compiler() {
		this.x86_64 = new X86_64();
		x86_64.dlabel("true");
		x86_64.string("True");
		x86_64.dlabel("false");
		x86_64.string("False");
		x86_64.dlabel("none");
		x86_64.string("None");
	}

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	@Override
	public void visit(Cnone c) {
		// Push 0 to the stack (falsy value)
		x86_64.movq("$0", Regs.RDI);
		x86_64.pushq(Regs.RDI);

		if (debug) {
			System.out.println("CNone");
		}
	}

	@Override
	public void visit(Cbool c) {

		// Push the boolean value on the stack
		x86_64.movq("$" + (c.b ? 1 : 0), Regs.RDI);
		x86_64.pushq(Regs.RDI);

		if (debug) {
			System.out.println("Cbool: " + c.b);
		}
	}

	@Override
	public void visit(Cstring c) {
		String label = newCstLabel();
		x86_64.dlabel(label);
		x86_64.string(c.s);

		// Push the string address on the stack
		x86_64.movq("$" + label, Regs.RDI);
		x86_64.pushq(Regs.RDI);

		if (debug) {
			System.out.println("Cstring: " + c.s + " -> " + label);
		}
	}

	@Override
	public void visit(Cint c) {

		// Directly push the integer value on the stack
		x86_64.movq("$" + c.i, Regs.RDI);
		x86_64.pushq(Regs.RDI);

		if (debug) {
			System.out.println("Cint: " + c.i);
		}
	}

	/**
	 * Visit a constant. This method redirects to the specific Constant visit methods
	 */
	@Override
	public void visit(TEcst e) {
		e.c.accept(this);
	}

	@Override
	public void visit(TEbinop e) {

		// 1. Accept the first expression. It will push its result to the stack
		e.e1.accept(this);
		// 2. Accept the second expression. It will push its result to the stack
		e.e2.accept(this);

		// Pop the two results from the stack onto usual registers
		x86_64.popq(Regs.RSI); // %rsi = 2nd value
		x86_64.popq(Regs.RDI); // %rdi = 1st value

		switch (e.op) {
			case Beq -> {
				x86_64.cmpq(Regs.RSI, Regs.RDI); // Compare the 2 values
				x86_64.sete("%cl"); // Set the low byte of register %rcx to 1 if they are equal, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Bsub -> x86_64.subq(Regs.RSI, Regs.RDI); // %rdi = %rdi - %rsi
			case Badd -> x86_64.addq(Regs.RSI, Regs.RDI); // %rdi = %rdi + %rsi
			case Band -> x86_64.andq(Regs.RSI, Regs.RDI); // %rdi = %rdi && %rsi
			case Bor -> x86_64.orq(Regs.RSI, Regs.RDI); // %rdi = %rdi || %rsi
			case Bmul -> x86_64.imulq(Regs.RSI, Regs.RDI); // %rdi = %rdi * %rsi
			case Bdiv -> {
				x86_64.movq(Regs.RDI, Regs.RAX); // Move %rdi to %rax
				x86_64.cqto(); // Sign extend %rax to %rdx:%rax
				x86_64.idivq(Regs.RSI); // Divide %rdx:%rax by %rsi
				x86_64.movq(Regs.RAX, Regs.RDI); // Move the result to %rdi
			}
			case Bmod -> {
				x86_64.movq(Regs.RDI, Regs.RAX); // Move %rdi to %rax
				x86_64.cqto(); // Sign extend %rax to %rdx:%rax
				x86_64.idivq(Regs.RSI); // Divide %rdx:%rax by %rsi
				x86_64.movq(Regs.RDX, Regs.RDI); // Move the remainder to %rdi
			}
			case Bneq -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setne("%cl"); // Set the low byte of register %rcx to 1 if they are different, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Blt -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setl("%cl"); // Set the low byte of register %rcx to 1 if the first is less than the second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Bgt -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setg("%cl"); // Set the low byte of register %rcx to 1 if the first is greater than the second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Ble -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setle("%cl"); // Set the low byte of register %rcx to 1 if the first is less or equal to the second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Bge -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setge("%cl"); // Set the low byte of register %rcx to 1 if the first is greater or equal to the second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
		}

		// Finally: push the result to the stack
		x86_64.pushq(Regs.RDI);
	}

	@Override
	public void visit(TEunop e) {

		// 1. Evaluate the expression. It will push its result to the stack
		e.e.accept(this);

		// 2. Pop the result from the stack onto a usual register
		x86_64.popq(Regs.RDI);

		switch (e.op) {
			case Uneg -> x86_64.negq(Regs.RDI); // %rdi = -%rdi
			case Unot -> x86_64.notq(Regs.RDI); // %rdi = !%rdi
		}

		// Finally: push the result to the stack
		x86_64.pushq(Regs.RDI);
	}

	@Override
	public void visit(TEident e) {
		// Access a variable value and push it to the stack in order to make it easily available for the next operation
		// NOTE: we assume that the internal variable's offset in comparison to %rbp has already been set.
		x86_64.movq(e.x.ofs + "(" + Regs.RBP + ")", Regs.RDI);
		x86_64.pushq(Regs.RDI);
	}

	@Override
	public void visit(TEcall e) {
		throw new Todo();
	}

	@Override
	public void visit(TEget e) {
		// 1. Evaluate the list expression address
		e.e1.accept(this);
		// 2. Evaluate the index expression
		e.e2.accept(this);

		// 3. Pop the index and list address from the stack into usual registers
		x86_64.popq(Regs.RSI); // %rsi = index
		x86_64.popq(Regs.RDI); // %rdi = list address

		// TODO: how to compare list length ? We need to allocate memory for lists and store their lengths

		// 4. Load the value at the index and push it to the stack
		x86_64.movq(Regs.RDI, Regs.RDI); // %rdi = list address
		x86_64.pushq(Regs.RDI);
	}

	@Override
	public void visit(TElist e) {
		throw new Todo();
	}

	@Override
	public void visit(TErange e) {
	}

	@Override
	public void visit(TSif s) {
		throw new Todo();
	}

	@Override
	public void visit(TSreturn s) {
		throw new Todo();
	}

	@Override
	public void visit(TSassign s) {

		// 1. Evaluate the value to be stored and push it to the stack
		s.e.accept(this);

		// 2. Pop the value from the stack into a usual register
		x86_64.popq(Regs.RDI);

		// 3. Store the value into the variable
		x86_64.movq(Regs.RDI, s.x.ofs + "(" + Regs.RBP + ")");
	}

	@Override
	public void visit(TSprint s) {

		// 1. Evaluate the value to be printed and push it to the stack
		s.e.accept(this);

		// TODO: we will need some typing analysis to determine what formatted string to use !
		// If this is a string, we just pushed an address onto the stack.
		// If it was and integer or a boolean, it would be a simple value that we can print straight away.

		// For now, we will assume that all values are strings. We will need to make a choice between in order to continue
		// * memory allocated values with the 1st byte being the type (string, int, bool, etc.)
		// * statically typed values (we need to store the types in TExpr) for which no checks are required.

		// TODO: switch depending on the type, or do dynamic type checks with memory allocated values
		// 2. Pop the value from the stack into a usual register
		// Because we assume that the value is a string, we know it is a string address and we can directly put it as 1st argument
		x86_64.popq(Regs.RDI);

		// Call printf
		printf();

		// Last: print a newline
		newline();

		if (debug) {
			System.out.println("TSprint -> " + s.e.getClass().getSimpleName());
		}
	}

	@Override
	public void visit(TSblock s) {
		for (TStmt stmt : s.l) {
			stmt.accept(this);
		}
	}

	@Override
	public void visit(TSfor s) {
		throw new Todo();
	}

	@Override
	public void visit(TSeval s) {
		throw new Todo();
	}

	@Override
	public void visit(TSset s) {
		throw new Todo();
	}

	// Helper methods

	/**
	 * Print a newline to standard output
	 */
	public void newline() {
		// Align stack to 16 bytes using a greedy method (sure to work, but this will "allocate" empty space on the stack)
		x86_64.andq("$-16", Regs.RSP);

		x86_64.movq("$10", Regs.RDI); // 10 is the code for '\n'
		x86_64.call("putchar");
	}

	/**
	 * Printf wrapper to align the stack and zero rax
	 */
	public void printf() {
		// Zero %rax
		x86_64.xorq(Regs.RAX, Regs.RAX);

		// Align the stack with a greedy method
		x86_64.andq("$-16", Regs.RSP);

		// Call printf
		x86_64.call("printf");
	}
}
