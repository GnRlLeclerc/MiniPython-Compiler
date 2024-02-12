package mini_python;

class Compiler implements TVisitor {

	static boolean debug = false;

	X86_64 x86_64;
	int cstId = 0;
	String currCstLabel;

	Compiler() {
		this.x86_64 = new X86_64();
	}

	private String newCstLabel() {
		return "cst" + this.cstId++;
	}

	@Override
	public void visit(Cnone c) {
		throw new Todo();
	}

	@Override
	public void visit(Cbool c) {
		throw new Todo();
	}

	@Override
	public void visit(Cstring c) {
		this.currCstLabel = newCstLabel();
		x86_64.dlabel(this.currCstLabel);
		x86_64.data(".string " + '"' + c.s + '"');
		if (debug) {
			System.out.println("Cstring: " + c.s + " -> " + this.currCstLabel);
		}
	}

	@Override
	public void visit(Cint c) {
		throw new Todo();
	}

	@Override
	public void visit(TEcst e) {
		if (e.c instanceof Cstring) {
			this.visit((Cstring) e.c);
			x86_64.movq("$" + currCstLabel, "%rdi");
			x86_64.movq(0, "%rax");
			x86_64.call("printf");
		} else {
			throw new Todo();
		}
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
		switch (s.e.getClass().getSimpleName()) {
			case "TEcst":
				TEcst e = (TEcst) s.e;
				e.accept(this);
				break;

			default:
				throw new Todo();
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
