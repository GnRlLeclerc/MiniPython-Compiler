package mini_python;

import mini_python.registers.Regs;

class Compiler implements TVisitor {

	static boolean debug = false;

	X86_64 x86_64;
	int cstId = 0;
	String currCstStringLabel;

	Compiler() {
		this.x86_64 = new X86_64();
		x86_64.dlabel("true");
		x86_64.string("True");
		x86_64.dlabel("false");
		x86_64.string("False");
		x86_64.dlabel("none");
		x86_64.string("None");
		x86_64.dlabel("newLine");
		x86_64.string("\n");
	}

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	@Override
	public void visit(Cnone c) {
		this.currCstStringLabel = "none";
		if (debug) {
			System.out.println("CNone");
		}
	}

	@Override
	public void visit(Cbool c) {

		this.currCstStringLabel = c.b ? "true" : "false";

		if (debug) {
			System.out.println("Cbool: " + c.b);
		}
	}

	@Override
	public void visit(Cstring c) {
		this.currCstStringLabel = newCstLabel();
		x86_64.dlabel(this.currCstStringLabel);
		x86_64.string(c.s);
		if (debug) {
			System.out.println("Cstring: " + c.s + " -> " + this.currCstStringLabel);
		}
	}

	@Override
	public void visit(Cint c) {
		throw new Todo();
	}

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
		x86_64.popq(Regs.RDI);
		x86_64.popq(Regs.RSI);

		switch (e.op) {
			case Beq -> {
				x86_64.cmpq(Regs.RDI, Regs.RSI); // Compare the 2 values
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
		throw new Todo();
	}

	@Override
	public void visit(TEident e) {
		throw new Todo();
	}

	@Override
	public void visit(TEcall e) {
		throw new Todo();
	}

	@Override
	public void visit(TEget e) {
		throw new Todo();
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
		throw new Todo();
	}

	@Override
	public void visit(TSprint s) {
		// TODO: better branching + see how to handle complex prints
		switch (s.e.getClass().getSimpleName()) {
			case "TEcst":
				TEcst e = (TEcst) s.e;
				e.accept(this);
				x86_64.movq("$" + currCstStringLabel, "%rdi");
				x86_64.movq(0, "%rax");
				x86_64.subq("$8", "%rsp");
				x86_64.call("printf");
				x86_64.addq("$8", "%rsp");
				break;

			default:
				throw new Todo();
		}
		x86_64.movq("$newLine", "%rdi");
		x86_64.movq(0, "%rax");
		x86_64.subq("$8", "%rsp");
		x86_64.call("printf");
		x86_64.addq("$8", "%rsp");
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
}
