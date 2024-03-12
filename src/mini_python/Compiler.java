package mini_python;

import java.util.List;

import mini_python.exception_handling.Todo;
import mini_python.libc.ExtendedLibc;
import mini_python.registers.Regs;
import mini_python.syntax.TDef;
import mini_python.syntax.constants.Cbool;
import mini_python.syntax.constants.Cint;
import mini_python.syntax.constants.Cnone;
import mini_python.syntax.constants.Cstring;
import mini_python.syntax.exprs_typed.TEbinop;
import mini_python.syntax.exprs_typed.TEcall;
import mini_python.syntax.exprs_typed.TEcst;
import mini_python.syntax.exprs_typed.TEget;
import mini_python.syntax.exprs_typed.TEident;
import mini_python.syntax.exprs_typed.TElist;
import mini_python.syntax.exprs_typed.TErange;
import mini_python.syntax.exprs_typed.TEunop;
import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.stmts_typed.TSassign;
import mini_python.syntax.stmts_typed.TSblock;
import mini_python.syntax.stmts_typed.TSeval;
import mini_python.syntax.stmts_typed.TSfor;
import mini_python.syntax.stmts_typed.TSif;
import mini_python.syntax.stmts_typed.TSprint;
import mini_python.syntax.stmts_typed.TSreturn;
import mini_python.syntax.stmts_typed.TSset;
import mini_python.syntax.stmts_typed.TStmt;
import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

class Compiler implements TVisitor {

	static boolean debug = false;
	// Reference to the current function being compiled. Used by TSreturn in order
	// to know the size of the local variables
	// to be cleaned up upon returning.
	protected TDef currentFunction;

	X86_64 x86_64;
	int cstId = 0;
	int jmpLabelId = 0;

	// Keep a trace of temporary values pushed to the stack (used in for loops, for
	// instance)
	// so that Return statements can clean up if they exit a non-trivial stack frame
	// prematurely
	// WARNING: this is only used for "for" loops. As we also use a stack alignment
	// strategy, we must not use an odd
	// number of temporary stack values.
	int tmpStackValuesCount = 0;

	Compiler() {
		this.x86_64 = new X86_64();
	}

	// ******************************************* CONSTANT VISIT
	// *************************************************** //

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	private String newJmpLabel() {
		return "jmp_" + this.jmpLabelId++;
	}

	@Override
	public void visit(Cnone c) {
		// Allocate a new None dynamic value into %rax
		alloc_none();

		// Push it to the stack
		x86_64.pushq(Regs.RAX);

		if (debug) {
			System.out.println("Allocation for CNone");
		}
	}

	@Override
	public void visit(Cbool c) {
		// Allocate a new bool dynamic value into %rax
		alloc_bool(c.b);

		// Push it to the stack
		x86_64.pushq(Regs.RAX);

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
		// Allocate and initialize a dynamic value to %rax
		alloc_int64(c.i);

		x86_64.pushq(Regs.RAX);

		if (debug) {
			System.out.println("Allocation for Cint: " + c.i);
		}
	}

	/**
	 * Allocate and initialize a dynamic value for containing strings.
	 * The value address is pushed to the stack
	 * The actual origin string is hard coded into the assembly data, but this
	 * allocation will copy it into the heap
	 */
	@Override
	public void visit(Cstring c) {

		// Hardcode the string into assembly
		String label = newCstLabel();
		x86_64.dlabel(label);
		x86_64.string(c.s);

		// Allocate and copy the string. The new address is in %r13, not %r12 because we
		// need to keep %r12 for the stack alignment
		alloc_string_from_label(label, c.s);

		// Push the string address on the stack
		x86_64.pushq(Regs.R13);

		if (debug) {
			System.out.println("Allocation for Cstring: \"" + c.s + "\" with data label \"" + label + '"');
		}
	}

	// ****************************************** EXPRESSIONS VISIT
	// ************************************************* //

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
		// Special case: lazy evaluation for AND and OR

		switch (e.op) {
			case Band -> {
				String next = newJmpLabel();
				// Accept the first value and push it to the stack
				e.e1.accept(this);

				String suffix_1 = "";
				if (e.e1.temporary) {
					suffix_1 = "_temp";
				}

				// Pop it to the 1st argument register
				x86_64.popq(Regs.RDI);

				// Call the truthy_dynamic extended libc function
				callExtendedLibc(ExtendedLibc.TRUTHY_DYNAMIC, suffix_1);

				// Compare the value to 0 and finish lazy evaluation if it is 0
				x86_64.cmpq(0, "9(%rax)");
				x86_64.jz(next); // If falsy, we can stop right there

				// Else, accept the second value and push it to the stack
				e.e2.accept(this);

				String suffix_2 = "";
				if (e.e2.temporary) {
					suffix_2 = "_temp";
				}

				// Pop it to the 1st argument register
				x86_64.popq(Regs.RDI);

				// Call the truthy_dynamic extended libc function, we will push the result
				// straight to the stack
				callExtendedLibc(ExtendedLibc.TRUTHY_DYNAMIC, suffix_2);

				x86_64.label(next);
				x86_64.pushq(Regs.RAX);

				return;
			}
			case Bor -> {
				String next = newJmpLabel();
				// Accept the first value and push it to the stack
				e.e1.accept(this);

				String suffix_1 = "";
				if (e.e1.temporary) {
					suffix_1 = "_temp";
				}

				// Pop it to the 1st argument register
				x86_64.popq(Regs.RDI);

				// Call the truthy_dynamic extended libc function
				callExtendedLibc(ExtendedLibc.TRUTHY_DYNAMIC, suffix_1);

				// Compare the value to 0 and finish lazy evaluation if it is 1
				x86_64.cmpq(1, "9(%rax)");
				x86_64.jz(next); // If truthy, we can stop right there

				// Else, accept the second value and push it to the stack
				e.e2.accept(this);

				String suffix_2 = "";
				if (e.e2.temporary) {
					suffix_2 = "_temp";
				}

				// Pop it to the 1st argument register
				x86_64.popq(Regs.RDI);

				// Call the truthy_dynamic extended libc function, we will push the result
				// straight to the stack
				callExtendedLibc(ExtendedLibc.TRUTHY_DYNAMIC, suffix_2);

				x86_64.label(next);
				x86_64.pushq(Regs.RAX);
				return;
			}
			default -> {
			}
		}

		// NOTE: constant expressions are already converted to allocated dynamic values.
		// We can assume that all expressions here are dynamic values.

		// 1. Accept the first expression. It will push its result to the stack
		e.e1.accept(this);
		// 2. Accept the second expression. It will push its result to the stack
		e.e2.accept(this);

		if (debug) {
			System.out.println("Binop: " + e.op + " " + e.type);
		}

		// Pop the two results from the stack onto usual registers
		x86_64.popq(Regs.RSI); // %rsi = 2nd value
		x86_64.popq(Regs.RDI); // %rdi = 1st value

		String suffix = "";
		if (e.e1.temporary && e.e2.temporary) {
			suffix = "_temp_3";
		} else if (e.e1.temporary) {
			suffix = "_temp_1";
		} else if (e.e2.temporary) {
			suffix = "_temp_2";
		}

		switch (e.op) {
			case Badd ->
				callExtendedLibc(ExtendedLibc.ADD_DYNAMIC, suffix);
			case Bsub ->
				callExtendedLibc(ExtendedLibc.SUB_DYNAMIC, suffix);
			case Blt ->
				callExtendedLibc(ExtendedLibc.LT_DYNAMIC, suffix);
			case Bmul ->
				callExtendedLibc(ExtendedLibc.MUL_DYNAMIC, suffix);
			case Bgt ->
				callExtendedLibc(ExtendedLibc.GT_DYNAMIC, suffix);
			case Bdiv ->
				callExtendedLibc(ExtendedLibc.DIV_DYNAMIC, suffix);
			case Bmod ->
				callExtendedLibc(ExtendedLibc.MOD_DYNAMIC, suffix);
			case Beq ->
				callExtendedLibc(ExtendedLibc.EQ_DYNAMIC, suffix);
			case Bneq ->
				callExtendedLibc(ExtendedLibc.NEQ_DYNAMIC, suffix);
			case Bge ->
				callExtendedLibc(ExtendedLibc.GE_DYNAMIC, suffix);
			case Ble ->
				callExtendedLibc(ExtendedLibc.LE_DYNAMIC, suffix);

			default -> throw new Todo("Binop: " + e.op);
		}

		// Finally: push the result to the stack
		x86_64.pushq(Regs.RAX);
	}

	@Override
	public void visit(TEunop e) {
		// 1. Evaluate the expression. It will push its result to the stack
		e.e.accept(this);

		// 2. Pop the result from the stack onto a usual register
		x86_64.popq(Regs.RDI);

		String suffix = "";
		if (e.e.temporary) {
			suffix = "_temp";
		}

		if (debug) {
			System.out.println("Unop: " + e.op);
		}

		switch (e.op) {
			case Uneg ->
				// Call the neg_dynamic extended libc function
				callExtendedLibc(ExtendedLibc.NEG_DYNAMIC, suffix);
			case Unot ->
				// Call the not_dynamic extended libc function
				callExtendedLibc(ExtendedLibc.NOT_DYNAMIC, suffix);
		}

		// Finally: push the result to the stack
		x86_64.pushq(Regs.RAX);
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
		x86_64.pushq(e.x.ofs + "(" + Regs.RBP + ")");
	}

	@Override
	public void visit(TEcall e) {
		// Special management of the list construct, only called with ranged
		if (e.f.name == "list") {
			e.l.getFirst().accept(this);
			x86_64.popq(Regs.RDI);
			callExtendedLibc(ExtendedLibc.RANGE_LIST);
			x86_64.pushq(Regs.RAX);
			return;
		}

		// Handle the len() function call
		if (e.f.name == "len") {
			// Evaluate the argument expression and push it to the stack (we do not check
			// whether it is a list or not)
			e.l.getFirst().accept(this);

			x86_64.popq(Regs.RDI); // Pop to the 1st argument register

			// Call the len_dynamic extended libc function
			callExtendedLibc(ExtendedLibc.LEN_DYNAMIC);

			// Push the result to the stack
			x86_64.pushq(Regs.RAX);
			return;
		}

		// 1. Accept all arguments and push them to the stack in reverse order
		// (this will be useful when we optimize the function call using registers or
		// even reusing this stack frame
		// instead of copying the arguments to the new stack frame.
		// When registering a function, the arguments are given increasing offsets from
		// the stack frame, in order.
		List<TExpr> exprList = e.l;
		int paramCount = exprList.size();
		boolean isOdd = paramCount % 2 == 1;

		if (isOdd) {
			// We need to align the stack to 16 bytes
			x86_64.pushq("$0");
		}

		for (int i = paramCount - 1; i >= 0; i--) {
			TExpr expr = exprList.get(i);
			expr.accept(this);
		}

		// 3. Call the function
		x86_64.call("func_" + e.f.name);

		// 4. Remove the arguments from the stack (and the padding for alignment if
		// needed if needed)
		x86_64.addq("$" + 8 * (e.l.size() + (isOdd ? 1 : 0)), Regs.RSP);

		// 5. Push the returned value to the stack
		x86_64.pushq(Regs.RAX);

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
		x86_64.popq(Regs.RSI); // %rsi = index (2nd argument)
		x86_64.popq(Regs.RDI); // %rdi = list address (1st argument)

		// Call extended libc for this computation
		callExtendedLibc(ExtendedLibc.GET_ELEMENT);

		// Push the result to the stack
		x86_64.pushq(Regs.RAX);
	}

	@Override
	public void visit(TElist e) {
		long size = e.l.size();

		if (debug) {
			System.out.println("Evaluating and allocating list of size " + size);
		}

		// Accept all statements in order and push them to the stack
		for (TExpr expr : e.l) {
			expr.accept(this);
		}

		alloc_list(size); // Allocate the list and set its address to %rax

		// Pop all values to the list in reverse order (the last element is currently
		// the first on the stack)
		// We also increment their reference count
		for (int i = 0; i < size; i++) {
			// Skip the 1 + 8 + 8 bytes of the tag, ref count, and length
			x86_64.popq(Regs.RDI); // %rdi = element address
			x86_64.incq("1(" + Regs.RDI + ")"); // Increment the reference count
			x86_64.movq(Regs.RDI, ((size - i - 1) * 8 + 17) + "(" + Regs.RAX + ")");
		}

		// Push the list address to the stack
		x86_64.pushq(Regs.RAX);
	}

	@Override
	public void visit(TErange e) {
		e.e.accept(this);
	}

	// ******************************************** STATEMENT VISIT
	// ************************************************* //

	@Override
	public void visit(TSif s) {
		// Define labels
		String falsy = newJmpLabel();
		String next = newJmpLabel();

		if (debug) {
			System.out.println("If / Else statement");
		}

		// Evaluate the expression and push it to the stack
		s.e.accept(this);

		// Pop it to the 1st argument register
		x86_64.popq(Regs.RDI);

		callExtendedLibc(ExtendedLibc.TRUTHY_DYNAMIC);

		// Compare the value to 0 and jump to the else block if it is 0
		x86_64.cmpq(0, "9(%rax)");
		x86_64.jz(falsy);

		// Truthy statement
		s.s1.accept(this);
		x86_64.jmp(next);

		// Falsy statement
		x86_64.label(falsy);
		s.s2.accept(this);

		// Next statement
		x86_64.label(next);
	}

	@Override
	public void visit(TSreturn s) {
		// The stack may have temporary values on top (such as for-loop index holders)
		// we will clean them up before beginning the return sequence
		if (this.tmpStackValuesCount > 0) {
			x86_64.addq("$" + 8 * this.tmpStackValuesCount, Regs.RSP);
		}

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

		if (debug) {
			System.out.println("Return of type " + s.e.getType() + " from function " + currentFunction.f.name);
		}
	}

	@Override
	public void visit(TSassign s) {
		// TODO: garbage collect the previous value if needed (do a helper method to
		// push the correct assembly inline)

		// There are 2 possibilities:

		// 1. This is a constant value being allocated to a dynamic variable.
		// 2. This is an assignment from an existing dynamic value.

		// In both cases, accepting the expression will result in pushing to the stack a
		// reference to a dynamic value,
		// whether it already existed or was just created. We only have to update its
		// reference count.

		// Accept the statement expression and push the returned value to the stack. Pop
		// it straight up to %rdi
		s.e.accept(this);
		x86_64.popq(Regs.RDI);

		// Increment the reference count and copy the address
		x86_64.incq("1(" + Regs.RDI + ")"); // Increment the reference count (skip the first tag byte)
		x86_64.movq(Regs.RDI, s.x.ofs + "(" + Regs.RBP + ")"); // Copy the address to the variable stack frame offset

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

		// 2. Pop the dynamic value address to the %rdi register
		// in order to pass it as an argument to println_dynamic extended libc function
		x86_64.popq(Regs.RDI);

		// 3. Call the println_dynamic extended libc function
		callExtendedLibc(ExtendedLibc.PRINTLN_DYNAMIC);

		if (debug) {
			System.out.println("Print -> " + s.e.getClass().getSimpleName() + " of type " + s.e.getType());
		}
	}

	@Override
	public void visit(TSblock s) {

		if (debug) {
			System.out.println("Statement block\n");
		}

		for (TStmt stmt : s.l) {
			stmt.accept(this);
			if (debug) {
				System.out.println();
			}
		}
	}

	@Override
	public void visit(TSfor s) {

		if (debug) {
			System.out.println("For loop evaluation\n");
		}

		// Create labels
		String loop = newJmpLabel();
		String continue_loop = newJmpLabel();
		String next = newJmpLabel();

		// Evaluate the list and push it to the stack
		s.e.accept(this);

		// NOTE: we use the callee-saved register %r14 in order to keep track of the
		// current index
		// NOTE: we use the callee-saved register %r15 in order to keep track of the
		// current list address
		// We do not use this register anywhere else in the code.
		x86_64.movq(0, Regs.R14); // Initialize the index to 0
		x86_64.popq(Regs.R15); // Pop the list address to %r15
		x86_64.label(loop);

		// Compare the current index to the list length
		x86_64.cmpq("9(%r15)", Regs.R14); // Compare the index to the list length
		x86_64.jl(continue_loop); // If index < length, continue the loop
		x86_64.jmp(next); // Jump to the next statement
		x86_64.label(continue_loop);

		// Alias the list value to the iteration variable
		x86_64.leaq("17(%r15, %r14, 8)", Regs.RDI); // Load the element address to the iteration variable
		x86_64.movq("(%rdi)", Regs.RDI);
		x86_64.movq(Regs.RDI, s.x.ofs + "(%rbp)");

		// Increment the index
		x86_64.incq(Regs.R14);

		// Evaluate the statement
		if (debug)
			System.out.println();

		x86_64.pushq(Regs.R14); // Push the index to the stack
		x86_64.pushq(Regs.R15); // Push the list address to the stack
		this.tmpStackValuesCount += 2; // Keep track of the number of temporary values pushed to the stack

		s.s.accept(this); // NOTE: this may modify the %r14 and %r15 registers

		x86_64.popq(Regs.R15); // Push the list address to the stack
		x86_64.popq(Regs.R14); // Push the index to the stack
		this.tmpStackValuesCount -= 2; // Keep track of the number of temporary values pushed to the stack

		// End: jump back to the loop evaluation
		x86_64.jmp(loop);
		x86_64.label(next);
	}

	// Helper methods

	@Override
	public void visit(TSeval s) {
		s.e.accept(this); // Accept the statement expression and push the returned value to the stack
		x86_64.popq(Regs.RAX); // Immediately pop the value, we will not use it
	}

	@Override
	public void visit(TSset s) {
		// TODO: garbage collect the previous value (and decrement its reference count)

		// Accept all statements and push them to the stack
		s.e1.accept(this); // List address
		s.e2.accept(this); // List index
		s.e3.accept(this); // Value

		x86_64.popq(Regs.RDX); // Value to %rdx (3rd argument)
		x86_64.popq(Regs.RSI); // Index to %rsi. This must be a dynamic int64 or bool value (2nd argument)
		x86_64.popq(Regs.RDI); // List address to %rdi (1st argument)

		// Call the set_element extended libc function
		callExtendedLibc(ExtendedLibc.SET_ELEMENT);
	}

	// **************************************************************************************************************
	// //
	// HELPER FUNCTIONS FOR ASSEMBLY CODE GENERATION //
	// **************************************************************************************************************
	// //

	// *********************************************** ALLOCATION
	// *************************************************** //

	/**
	 * Allocate a dynamic value for a given type that has a fixed size (not string
	 * or list)
	 * Prefills its type tag with the correct one, the reference count with 0, and
	 * returns the memory pointer in %rax.
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

		// Initialize the ref count to 0 (the allocated value has not been assigned to
		// any variable yet)
		x86_64.movq(0, "1(%rax)");
	}

	/**
	 * Allocate a dynamic int64 value.
	 * The int64 byte tag is 2.
	 * This function will put the memory pointer in %rax because this is where
	 * malloc returns the pointer.
	 */
	private void alloc_int64() {
		alloc_known_size(Type.INT64);
	}

	/**
	 * Allocate a dynamic int64 value with an initial value
	 * The int64 byte tag is 2.
	 * This function will put the memory pointer in %rax because this is where
	 * malloc returns the pointer.
	 */
	private void alloc_int64(long value) {
		alloc_int64();
		// Store the value in the dynamic value, skipping the first tag byte and the ref
		// count
		x86_64.movq("$" + value, "9(%rax)");
	}

	/**
	 * Allocate a dynamic None value.
	 * No value is needed, just the tag. The None tag is 0
	 * This function will put the memory pointer in %rax because this is where
	 * malloc returns the pointer.
	 */
	private void alloc_none() {
		alloc_known_size(Type.NONETYPE);
	}

	/**
	 * Allocate a dynamic bool value.
	 * The bool byte tag is 1.
	 * This function will put the memory pointer in %rax.
	 */
	private void alloc_bool() {
		alloc_known_size(Type.BOOL);
	}

	private void alloc_bool(boolean value) {
		alloc_bool();
		// Store the value in the dynamic value, skipping the first tag byte and the ref
		// count
		x86_64.movq(value ? 1 : 0, "9(%rax)");
	}

	/**
	 * Allocate a dynamic string value that copies a hardcoded string.
	 * The string tag is 3
	 * This function will put the memory pointer in %r13 because we call strcpy
	 * which will update the value of %rax,
	 * and %r12 is used for stack alignment.
	 */
	private void alloc_string_from_label(String label, String value) {

		// Because the string is known at compile time, we do not need to compute its
		// size dynamically.
		// 1 tag byte + 8 ref count + 8 length + string length + 1 null byte
		long stringBytes = value.getBytes().length + 1;
		long byteSize = 1 + 8 + 8 + 8 + 2 * stringBytes + 7;

		x86_64.movq("$" + byteSize, Regs.RDI); // Allocate memory for the dynamic value
		callLibc("malloc");

		// The pointer result is in %rax. We need to put the type tag in the first byte
		x86_64.movq("$3", Regs.RDI);
		x86_64.mov("%dil", "(%rax)"); // See https://stackoverflow.com/a/65527553

		// Initialize the ref count to 0 (the allocated value has not been assigned to
		// any variable yet)
		x86_64.movq(0, "1(%rax)");

		// Initialize the length (remove the null termination byte)
		x86_64.movq("$" + (stringBytes - 1), "9(%rax)");
		// Initialize the length (remove the null termination byte)
		x86_64.movq("$" + (2 * stringBytes + 7), "17(%rax)");

		// Copy the string
		x86_64.leaq("25(%rax)", Regs.RDI); // %rdi = %rax + 1 + 8 + 8 // Move the destination address to %rdi
		x86_64.movq("$" + label, Regs.RSI); // Move the source address to %rsi
		x86_64.movq(Regs.RAX, Regs.R13); // Move the actual address of the string to a callee-saved register
		callLibc("strcpy"); // Note: this will update the value of rax !

		// The newly allocated and copied value is in %r13 (not %r12 because we need to
		// keep %r12 for the stack alignment)
	}

	/**
	 * Allocate a dynamic list value with a hardcoded length.
	 * The list tag is 4
	 * This function will put the memory pointer in %rax because this is where
	 * malloc returns the pointer.
	 */
	private void alloc_list(long size) {
		// Allocate memory for the dynamic value
		// 1 byte for the tag, 8 bytes for the refcount, 8 bytes for the length, and
		// size * 8 bytes for the element pointers
		x86_64.movq("$" + 1 + 8 + 8 + size * 8, Regs.RDI); // Allocate memory for the dynamic value
		callLibc("malloc");

		// The pointer result is in %rax. We need to put the type tag in the first byte
		x86_64.movq("$4", Regs.RDI);
		x86_64.mov("%dil", "(%rax)"); // See https://stackoverflow.com/a/65527553

		// Initialize the ref count to 0 (the allocated value has not been assigned to
		// any variable yet)
		x86_64.movq(0, "1(%rax)");

		// Initialize the length
		x86_64.movq("$" + size, "9(%rax)");
	}

	// ******************************************* LIBC CALL HELPERS
	// ************************************************ //

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
	private void callExtendedLibc(ExtendedLibc function, String suffix) {
		alignStack();
		x86_64.call(function.getLabel() + suffix);
		unalignStack();
	}

	private void callExtendedLibc(ExtendedLibc function) {
		callExtendedLibc(function, "");
	}

	// ******************************************* STACK ALIGNMENT
	// ************************************************* //

	// NOTE ABOUT STACK ALIGNMENT

	// PRELIMINARY NOTES

	// We only do pushq and popq, which push and pop 8-byte values to the stack.
	// This means that the stack is either aligned to 16 bytes, or to 8 bytes (quite
	// simple).

	// A statement consumes every stack value that it pushed durig evaluation: it
	// preserves the stack alignment.
	// This means that we do not have to keep track of branching, as no branches or
	// statements will permanently misalign the stack.

	// POSSIBLE STRATEGIES

	// DYNAMIC ALIGNMENT (the current one)

	// Because we pass all arguments through the stack, it is impossible to align
	// the stack "before" calling a function
	// and after having pushed the arguments on the stack (this is done by
	// "evaluating" them),
	// as it might make the first argument not be at 16(%rbp) anymore because of the
	// padding (it would be at 24(%rbp) instead).

	// This means that our own functions can be called with arbitrary stack
	// alignment.
	// We cannot statically determine the stack alignment upon entering a function.

	// Thus, we use the following strategy:
	// * only align the stack when calling libc and extended libc functions
	// * align the stack by computing the current alignment, storing the offset in a
	// callee-saved register,
	// ...and then restoring it from this same callee-saved register upon returning
	// from the function.

	// STATIC ALIGNMENT (could be slightly more optimized ?)

	// If we want to be able to statically determine if the stack is aligned or not,
	// we need to have the stack aligned
	// before calling any of our own functions. This poses an issue when pushing an
	// odd number of arguments to the stack,
	// as alignment will introduce a breaking 8-byte padding.

	// A solution is to have all of our functions FORCIBLY take an even number of
	// arguments. When calling functions
	// with an odd number of arguments, we pushq before evaluating the first
	// argument a phantom 8-byte value to the stack

	// This means that we spend less time computing the current stack alignment and
	// storing it, and we align it with a simple
	// pushq. Very nice ! Warning: take into account the main function allocating
	// space for its own local variables !

	// ===> This way, no stack particular stack alignment must be performed, as the
	// natural way of doing everything
	// ===> will keep the stack aligned to 16 bytes.

	/**
	 * Align the stack to 16 bytes by allocating enough space given the current
	 * stack alignment offset.
	 * The current offset is stored in the callee-saved register %r12 for
	 * restoration upon returning.
	 */
	private void alignStack() {
		// Compute the current stack alignment offset and store it in a callee-saved
		// register, %r12
		// x86_64.movq(Regs.RSP, Regs.R12);
		// x86_64.andq("$8", Regs.R12); // %r12 will either be 0 (aligned stack) or 8
		// (misaligned stack by 8 bytes)
		// x86_64.subq(Regs.R12, Regs.RSP); // Allocate enough space to align the stack
		// to 16 bytes
	}

	/**
	 * Unalign the stack and restore its current offset by reading the callee-saved
	 * register %r12
	 * THIS FUNCTION MUST BE CALLED IMMEDIATELY AFTER CALLING A LIBC FUNCTION.
	 * <p>
	 * It might pop the first stack value, so do not calling when the stack top
	 * value is not padding.
	 */
	private void unalignStack() {
		// Restore the stack alignment offset.
		// x86_64.addq(Regs.R12, Regs.RSP);
	}

}
