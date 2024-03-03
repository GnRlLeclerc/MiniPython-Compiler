package mini_python;

import mini_python.libc.ExtendedLibc;
import mini_python.registers.Registers;
import mini_python.registers.Regs;
import mini_python.typing.Type;

class Compiler implements TVisitor {

	static boolean debug = false;
	// Reference to the current function being compiled. Used by TSreturn in order to know the size of the local variables
	// to be cleaned up upon returning.
	protected TDef currentFunction;
	// Store the state of the stack alignment. When using a popq or pushq
	// instruction, increment or decrement this value.
	// It will be used to align the stack to 16 bytes when doing system calls.
	// It indicates a byte count and is always positive
	protected int stackAlignOffset = 0;
	X86_64 x86_64;
	int cstId = 0;

	Compiler() {
		this.x86_64 = new X86_64();
	}

	// ******************************************* CONSTANT VISIT *************************************************** //

	// Allocation helpers


	/**
	 * Allocate a dynamic bool value.
	 * The bool byte tag is 1.
	 * This function will put the memory pointer in %rax.
	 */
	private void alloc_bool() {
		// For an bool, we will need 1 byte for the type tag, and 8 bytes for the value (we store them as int64).
		x86_64.movq(9, Regs.RDI);

		callLibc("malloc");

		// The pointer result is in %rax. We need to put 1 in the first byte
		x86_64.movq(1, Regs.RDI);
		x86_64.mov("%dil", "(%rax)"); // See https://stackoverflow.com/a/65527553
	}


	/**
	 * Helper: convert the stack top value from a static type to a dynamic type
	 * TODO: remove this method, we will not need it.
	 */
	private void staticToDynamic(Type type) {

		switch (type) {
			case Type.NONETYPE -> // Allocate memory for the dynamic value
					alloc_none();
			case Type.BOOL -> {
				// Allocate memory for the dynamic value
				alloc_bool();
				// Store the static value in the dynamic value, skipping the first tag byte
				x86_64.popq(Regs.RDI); // Pop the static value from the stack
				x86_64.movq(Regs.RDI, "1(%rax)");
			}
			case Type.INT64 -> {
				// Allocate memory for the dynamic value
				alloc_int64();
				// Store the static value in the dynamic value, skipping the first tag byte
				x86_64.popq(Regs.RDI); // Pop the static value from the stack
				x86_64.movq(Regs.RDI, "1(%rax)");
			}
			default -> throw new Todo("staticToDynamic for static type " + type);
		}

		// Last: push the newly allocated dynamic value to the stack
		x86_64.pushq(Regs.RAX);
	}

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	@Override
	public void visit(Cnone c) {
		// Allocate a new None dynamic value into %rax
		alloc_none();

		// Push it to the stack
		x86_64.pushq(Regs.RAX);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Allocation for CNone");
		}
	}

	@Override
	public void visit(Cbool c) {
		// Allocate a new bool dynamic value into %rax
		alloc_bool();

		// Store the boolean value in the dynamic value, skipping the first tag byte
		x86_64.movq(c.b ? 1 : 0, "1(%rax)");

		// Push it to the stack
		x86_64.pushq(Regs.RAX);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Allocation for Cbool: " + c.b);
		}
	}


	/**
	 * Allocate and initialize a dynamic value for containing integers.
	 * The value address is pushed to the stack
	 */
	@Override
	public void visit(Cint c) {

		alloc_int64(c.i); // Allocate and initialize a dynamic value to %rax
		x86_64.pushq(Regs.RAX);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Allocation for Cint: " + c.i);
		}
	}


	/**
	 * Allocate and initialize a dynamic value for containing strings.
	 * The value address is pushed to the stack
	 * The actual origin string is hard coded into the assembly data, but this allocation will copy it into the heap
	 */
	@Override
	public void visit(Cstring c) {

		// Hardcode the string into assembly
		String label = newCstLabel();
		x86_64.dlabel(label);
		x86_64.string(c.s);

		// Allocate and copy the string
		alloc_string_from_label(label, c.s);

		// Push the string address on the stack
		x86_64.pushq(Regs.RAX);
		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Allocation for Cstring: \"" + c.s + "\" with data label \"" + label + '"');
		}
	}

	// ****************************************** EXPRESSIONS VISIT ************************************************* //


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

		// Reset the type of this statement to that of the variable, which may have been
		// updated with assignments
		e.type = e.x.type;

		if (debug) {
			System.out.println("Variable access: " + e.x.name + " of type " + e.getType() + " (uid: " + e.x.uid + ")"
					+ " with stack frame offset of " + e.x.ofs + " bytes");
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
		// 1. Accept all arguments and push them to the stack in reverse order
		int currArg = e.l.size() - 1;
		for (TExpr expr : e.l.reversed()) {
			expr.accept(this);

			// Convert the argument values from static to dynamic if needed
			if (expr.type != Type.DYNAMIC && e.f.params.get(currArg).type == Type.DYNAMIC) {
				// We need to convert the static value to a dynamic one
				staticToDynamic(expr.type);

			} else if (expr.type == Type.DYNAMIC && e.f.params.get(currArg).type != Type.DYNAMIC) {
				// We need to convert the dynamic value to a static one
				// We will pop the dynamic value from the stack and store it in a static variable
				// Then, we will push the static value to the stack
				throw new Todo("Dynamic to static conversion");
			}

			currArg--;
		}
		stackAlignOffset += e.l.size(); // e.l.size() pushed values

		// 2. Pass and pop the first 6 arguments using registers
		int argCount = e.l.size();
		for (int i = 0; i < Math.min(6, argCount); i++) {
			x86_64.popq(Registers.argReg(i).getCode());
			stackAlignOffset -= 1; // 1 popped value
		}

		// 3. Call the function
		alignStack();
		x86_64.call("func_" + e.f.name);
		unalignStack();

		// 4. Push the returned value to the stack
		x86_64.pushq(Regs.RAX);

		stackAlignOffset += 1; // 1 pushed value

		if (debug) {
			System.out.println("Function call: " + e.f.name + " with " + e.l.size() + " argument(s)");
		}
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

		// 1. Evaluate the return expression and push it to the stack
		s.e.accept(this);

		// 2. Pop the return value from the stack into the usual return register
		x86_64.popq(Regs.RAX);

		// 3. Free the stack allocated memory
		x86_64.addq("$" + -this.currentFunction.f.localVariablesOffset, Regs.RSP);

		// 4. Restore the stack frame
		x86_64.popq(Regs.RBP);

		// 5. Return from the function
		x86_64.ret();

		// We popped the stack frame and freed all the local variables
		this.stackAlignOffset -= 1 - this.currentFunction.f.localVariablesOffset / 8;

		if (debug) {
			System.out.println("Return of type " + s.e.getType() + " from function " + currentFunction.f.name);
		}
	}

	@Override
	public void visit(TSassign s) {
		// TODO: garbage collect the previous value if needed (do a helper method to push the correct assembly inline)

		// There are 2 possibilities:

		// 1. This is a constant value being allocated to a dynamic variable.
		// 2. This is an assignment from an existing dynamic value.

		// In both cases, accepting the expression will result in pushing to the stack a reference to a dynamic value,
		// whether it already existed or was just created. We only have to update its reference count.

		// Accept the statement expression and push the returned value to the stack. Pop it straight up to %rdi
		s.e.accept(this);
		x86_64.popq(Regs.RDI);

		// Copy the address and increment the reference count
		x86_64.movq(Regs.RDI, s.x.ofs + "(" + Regs.RBP + ")");
		x86_64.incq(s.x.ofs + 1 + "(" + Regs.RBP + ")"); // Increment the reference count (skip the first tag byte)

		if (debug) {
			System.out.println("Variable assignment: " + s.x.name + " (uid: "
					+ s.x.uid + ")" + " with stack frame offset of " + s.x.ofs + " bytes, from a "
					+ ((s.e instanceof TEcst) ? "constant" : "dynamic") + " value");
		}

	}

	@Override
	public void visit(TSprint s) {

		// 1. Evaluate the value to be printed and push it to the stack
		s.e.accept(this);

		// 2. Pop the value to the %rdi register in order to pass it as an argument to
		// the correct extended_libc function for printing
		x86_64.popq(Regs.RDI); // %rdi = string address or int value, as the 1st argument. If None, this is
		// useless and won't do any wrong
		stackAlignOffset -= 1; // 1 popped value

		if (debug) {
			System.out.println("Print -> " + s.e.getClass().getSimpleName() + " of type " + s.e.getType());
		}

		switch (s.e.getType()) {
			case Type.STRING -> callExtendedLibc(ExtendedLibc.PRINTLN_STRING);

			case Type.INT64 -> callExtendedLibc(ExtendedLibc.PRINTLN_INT64);

			case Type.NONETYPE -> callExtendedLibc(ExtendedLibc.PRINTLN_NONE);

			case Type.BOOL -> callExtendedLibc(ExtendedLibc.PRINTLN_BOOL);

			case Type.DYNAMIC -> callExtendedLibc(ExtendedLibc.PRINTLN_DYNAMIC);

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
		s.e.accept(this); // Accept the statement expression and push the returned value to the stack
		x86_64.popq(Regs.RAX); // Immediately pop the value, we will not use it
	}

	@Override
	public void visit(TSset s) {
		throw new Todo("TSet");
	}

	// ************************************************************************************************************** //
	//                                 HELPER FUNCTIONS FOR ASSEMBLY CODE GENERATION                                  //
	// ************************************************************************************************************** //

	// *********************************************** ALLOCATION *************************************************** //

	/**
	 * Allocate a dynamic value for a given type that has a fixed size (not string or list)
	 * Prefills its type tag with the correct one, the reference count with 0, and returns the memory pointer in %rax.
	 * (because this is where malloc returns the pointer)
	 */
	private void alloc_known_size(Type type) {
		int byteSize = switch (type) {
			case NONETYPE -> 1 + 8;
			case BOOL, INT64 -> 1 + 8 + 8; // 1 tag byte + 8 ref count + 8 value
			default -> throw new Error("Type " + type + " cannot be allocated with a known fixed size");
		};

		x86_64.movq(byteSize, Regs.RDI); // Allocate memory for the dynamic value
		callLibc("malloc");

		// The pointer result is in %rax. We need to put the type tag in the first byte
		x86_64.movq(type.typeTag(), Regs.RDI);
		x86_64.mov("%dil", "(%rax)"); // See https://stackoverflow.com/a/65527553

		// Initialize the ref count to 0 (the allocated value has not been assigned to any variable yet)
		x86_64.movq(0, "1(%rax)");
	}

	/**
	 * Allocate a dynamic int64 value.
	 * The int64 byte tag is 2.
	 * This function will put the memory pointer in %rax because this is where malloc returns the pointer.
	 */
	private void alloc_int64() {
		alloc_known_size(Type.INT64);
	}

	/**
	 * Allocate a dynamic int64 value with an initial value
	 * The int64 byte tag is 2.
	 * This function will put the memory pointer in %rax because this is where malloc returns the pointer.
	 */
	private void alloc_int64(long value) {
		alloc_int64();
		// Store the value in the dynamic value, skipping the first tag byte and the ref count
		x86_64.movq("$" + value, "9(%rax)");
	}


	/**
	 * Allocate a dynamic None value.
	 * No value is needed, just the tag. The None tag is 0
	 * This function will put the memory pointer in %rax because this is where malloc returns the pointer.
	 */
	private void alloc_none() {
		alloc_known_size(Type.NONETYPE);
	}

	/**
	 * Allocate a dynamic string value that copies a hardcoded string.
	 * The string tag is 3
	 * This function will put the memory pointer in %rax because this is where malloc returns the pointer.
	 */
	private void alloc_string_from_label(String label, String value) {

		// Because the string is known at compile time, we do not need to compute its size dynamically.
		// 1 tag byte + 8 ref count + 8 length + string length + 1 null byte
		long byteSize = 1 + 8 + 8 + value.length() + 1;

		x86_64.movq("$" + byteSize, Regs.RDI); // Allocate memory for the dynamic value
		callLibc("malloc");

		// The pointer result is in %rax. We need to put the type tag in the first byte
		x86_64.movq("$3", Regs.RDI);
		x86_64.mov("%dil", "(%rax)"); // See https://stackoverflow.com/a/65527553

		// Initialize the ref count to 0 (the allocated value has not been assigned to any variable yet)
		x86_64.movq(0, "1(%rax)");

		// Initialize the length
		x86_64.movq("$" + value.length(), "9(%rax)");

		// Copy the string
		x86_64.leaq("17(%rax)", Regs.RDI); // %rdi = %rax + 1 + 8 + 8 // Move the destination address to %rdi
		x86_64.movq("$" + label, Regs.RSI); // Move the source address to %rsi
		callLibc("strcpy");

		// The newly allocated and copied value is in %rax.
	}

	// ******************************************* LIBC CALL HELPERS ************************************************ //


	/**
	 * Call a function from the standard libc, and handle stack alignment
	 */
	private void callLibc(String function) {
		alignStack();
		x86_64.call(function);
		unalignStack();
	}

	/**
	 * Call a function from the extended libc, and handle stack alignment
	 */
	private void callExtendedLibc(ExtendedLibc function) {
		alignStack();
		x86_64.call(function.getLabel());
		unalignStack();
	}

	// ******************************************* STACK ALIGNMENT ************************************************* //


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


}
