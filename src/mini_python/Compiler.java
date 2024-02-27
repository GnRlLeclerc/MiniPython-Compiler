package mini_python;

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
		throw new Todo();
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
