package mini_python;

class Compiler implements TVisitor {

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
		throw new Todo();

	}

	@Override
	public void visit(Cint c) {
		throw new Todo();

	}

	@Override
	public void visit(TEcst e) {
		throw new Todo();

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
		throw new Todo();

	}

	@Override
	public void visit(TSblock s) {
		throw new Todo();

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
