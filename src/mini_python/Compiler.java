package mini_python;

import mini_python.libc.ExtendedLibc;
import mini_python.registers.Regs;
import mini_python.typing.Type;

class Compiler implements TVisitor {

	static boolean debug = false;
	X86_64 x86_64;
	int cstId = 0;
	// Store the state of the stack alignment. When using a popq or pushq
	// instruction, increment or decrement this value.
	// It will be used to align the stack to 16 bytes when doing system calls.
	// It indicates a byte count and is always positive
	private int stackAlignOffset = 0;

	Compiler() {
		this.x86_64 = new X86_64();
	}

	/**
	 * Align the stack to 16 bytes by allocating enough space given the current
	 * stack alignment offset
	 */
	private void alignStack() {
		stackAlignOffset = stackAlignOffset % 16;
		if (stackAlignOffset == 0) {
			return; // No need to align the stack
		}

		x86_64.subq("$" + (16 - stackAlignOffset), Regs.RSP);
	}

	/**
	 * Unalign the stack and restore its current offset
	 */
	private void unalignStack() {
		if (stackAlignOffset == 0) {
			return; // No need to unalign the stack
		}

		// Unalign the stack to its original offset
		x86_64.addq("$" + (16 - stackAlignOffset), Regs.RSP);
	}

	/**
	 * Call a function from the extended libc, and handle stack alignment
	 */
	private void callExtendedLibc(ExtendedLibc function) {
		alignStack();
		x86_64.call(function.getLabel());
		unalignStack();
	}

	/**
	 * Print a newline to standard output
	 */
	public void newline() {
		alignStack();

		x86_64.movq("$10", Regs.RDI); // 10 is the code for '\n'
		x86_64.call("putchar");

		unalignStack();
	}

	/**
	 * Printf wrapper to align the stack and zero rax
	 */
	public void printf() {
		// Zero %rax
		x86_64.xorq(Regs.RAX, Regs.RAX);

		alignStack();

		// Call printf
		x86_64.call("printf");

		unalignStack();
	}

	/**
	 * Printf wrapper that also prints a newline
	 */
	public void printfln() {

		// Zero %rax
		x86_64.xorq(Regs.RAX, Regs.RAX);

		alignStack();

		// Call printf
		x86_64.call("printf");

		// Call putchar
		x86_64.movq("$10", Regs.RDI); // 10 is the code for '\n'
		x86_64.call("putchar");

		unalignStack();
	}

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	@Override
	public void visit(Cnone c) {
		// Push 0 to the stack (falsy value)
		x86_64.movq("$0", Regs.RDI);
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("CNone");
		}
	}

	@Override
	public void visit(Cbool c) {

		// Push the boolean value on the stack
		x86_64.movq("$" + (c.b ? 1 : 0), Regs.RDI);
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value

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
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Cstring: " + c.s + " -> " + label);
		}
	}

	@Override
	public void visit(Cint c) {

		// Directly push the integer value on the stack
		x86_64.movq("$" + c.i, Regs.RDI);
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Cint: " + c.i);
		}
	}

	/**
	 * Visit a constant. This method redirects to the specific Constant visit
	 * methods
	 */
	@Override
	public void visit(TEcst e) {
		e.c.accept(this);
	}

	@Override
	public void visit(TEbinop e) {

		if (debug) {
			System.out.println("Binop: " + e.op + " " + e.type);
		}

		// 1. Accept the first expression. It will push its result to the stack
		e.e1.accept(this);
		// 2. Accept the second expression. It will push its result to the stack
		e.e2.accept(this);

		// Pop the two results from the stack onto usual registers
		x86_64.popq(Regs.RSI); // %rsi = 2nd value
		x86_64.popq(Regs.RDI); // %rdi = 1st value
		stackAlignOffset -= 2; // 2 popped values

		// TODO: this implementation only works with integers and boolean. We must
		// implement it for all types (string, list...)
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
				x86_64.setl("%cl"); // Set the low byte of register %rcx to 1 if the first is less than the second,
				// 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Bgt -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setg("%cl"); // Set the low byte of register %rcx to 1 if the first is greater than the
				// second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Ble -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setle("%cl"); // Set the low byte of register %rcx to 1 if the first is less or equal to the
				// second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
			case Bge -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
				x86_64.setge("%cl"); // Set the low byte of register %rcx to 1 if the first is greater or equal to
				// the second, 0 else
				x86_64.movzbq("%cl", Regs.RDI); // Move this byte to %rdi and extend it with zeroes
			}
		}

		// Finally: push the result to the stack
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value
	}

	@Override
	public void visit(TEunop e) {

		if (debug) {
			System.out.println("Unop: " + e.op);
		}

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
		// We popped and then pushed the same value, so the stack offset remains the
		// same
	}

	@Override
	public void visit(TEident e) {

		// Reset the type of this statement to that of the variable, which may have been updated with assignments
		e.type = e.x.type;

		if (debug) {
			System.out.println("Variable access: " + e.x.name + " of type " + e.getType() + " (uid: " + e.x.uid + ")" + " with stack frame offset of " + e.x.ofs + " bytes");
		}

		if (e.type == null) {
			throw new Error("Variable " + e.x.name + " is being accessed before assignment");
		}

		// Access a variable value and push it to the stack in order to make it easily
		// available for the next operation
		x86_64.movq(e.x.ofs + "(" + Regs.RBP + ")", Regs.RDI);
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value
	}

	@Override
	public void visit(TEcall e) {
		throw new Todo("TEcall");
	}

	@Override
	public void visit(TEget e) {

		if (debug) {
			System.out.println("List element access");
		}

		// 1. Evaluate the list expression address
		e.e1.accept(this);
		// 2. Evaluate the index expression
		e.e2.accept(this);

		// 3. Pop the index and list address from the stack into usual registers
		x86_64.popq(Regs.RSI); // %rsi = index
		x86_64.popq(Regs.RDI); // %rdi = list address
		stackAlignOffset -= 2; // 2 popped values

		// TODO: how to compare list length ? We need to allocate memory for lists and
		// store their lengths

		// 4. Load the value at the index and push it to the stack
		x86_64.movq(Regs.RDI, Regs.RDI); // %rdi = list address
		x86_64.pushq(Regs.RDI);
		stackAlignOffset += 1; // 1 pushed value
	}

	@Override
	public void visit(TElist e) {
		throw new Todo("TElist");
	}

	@Override
	public void visit(TErange e) {
	}

	@Override
	public void visit(TSif s) {
		throw new Todo("TSif");
	}

	@Override
	public void visit(TSreturn s) {
		throw new Todo("TSreturn");
	}

	@Override
	public void visit(TSassign s) {

		if (debug) {
			System.out.println("Variable assignment: " + s.x.name + " with new type " + s.e.getType() + " (uid: " + s.x.uid + ")" + " with stack frame offset of " + s.x.ofs + " bytes");
		}

		// 1. Evaluate the value to be stored and push it to the stack
		s.e.accept(this);

		// 2. Pop the value from the stack into a usual register
		x86_64.popq(Regs.RDI);
		stackAlignOffset -= 1; // 1 popped value

		// 3. We just assigned a value to the variable: update the type of the variable
		s.x.type = s.e.getType();


		// 4. Store the value into the variable
		x86_64.movq(Regs.RDI, s.x.ofs + "(" + Regs.RBP + ")");
	}

	@Override
	public void visit(TSprint s) {

		// 1. Evaluate the value to be printed and push it to the stack
		s.e.accept(this);

		if (debug) {
			System.out.println("Print -> " + s.e.getClass().getSimpleName() + " of type " + s.e.getType());
		}

		switch (s.e.getType()) {
			case Type.STRING -> {
				x86_64.popq(Regs.RDI); // %rdi = string address, as the 1st argument
				stackAlignOffset -= 1; // 1 popped value

				callExtendedLibc(ExtendedLibc.PRINTLN_STRING);
			}
			case Type.INT64 -> {
				x86_64.popq(Regs.RDI); // %rdi = int value, as the 1st argument
				stackAlignOffset -= 1; // 1 popped value

				callExtendedLibc(ExtendedLibc.PRINTLN_INT64);
			}
			case Type.NONETYPE -> {
				x86_64.popq(Regs.RSI); // %rsi = None value. This is useless, but we still need to pop it from the
				// stack
				stackAlignOffset -= 1; // 1 popped value

				callExtendedLibc(ExtendedLibc.PRINTLN_NONE);
			}
			case Type.BOOL -> {
				x86_64.popq(Regs.RDI); // %rdi = bool value, as the first argument
				stackAlignOffset -= 1; // 1 popped value

				callExtendedLibc(ExtendedLibc.PRINTLN_BOOL);
			}

			// TODO: for dynamic types, we will need to check the type at runtime, and call
			// the correct variation of printf
			// do a wrapper called "dynamicPrintf" in order to isolate this ?
			default -> throw new Todo("TSprint");
		}
	}

	@Override
	public void visit(TSblock s) {

		if (debug) {
			System.out.println("Statement block");
		}

		for (TStmt stmt : s.l) {
			stmt.accept(this);
		}
	}

	@Override
	public void visit(TSfor s) {
		throw new Todo("TSfor");
	}

	// Helper methods

	@Override
	public void visit(TSeval s) {
		throw new Todo("TSeval");
	}

	@Override
	public void visit(TSset s) {
		throw new Todo("TSet");
	}
}
