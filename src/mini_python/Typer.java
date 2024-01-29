package mini_python;

import java.util.HashMap;

// the following exception is used whenever you have to implement something
class Todo extends Error {
	private static final long serialVersionUID = 1L;

	Todo() {
		super("TODO");
	}
}

/* The typer starts here */
class Typer implements Visitor {

	// functions definitions (functions are global, hence `static`)
	static HashMap<String, Def> functions = new HashMap<>();
	static TFile tFile = new TFile();
	// local variables
	public HashMap<String, Variable> vars;

	Typer() {
		this.vars = new HashMap<>();
	}

	// interpreting constants is immediate
	public void visit(Cnone c) {
		throw new Todo();
	}

	public void visit(Cbool c) {
		throw new Todo();
	}

	public void visit(Cstring c) {
		throw new Todo();
	}

	public void visit(Cint c) {
		throw new Todo();
	}

	// interpreting expressions

	@Override
	public void visit(Ecst e) {
		throw new Todo();
	}

	@Override
	public void visit(Ebinop e) {
		throw new Todo();
	}

	@Override
	public void visit(Eunop e) {
		throw new Todo();
	}

	@Override
	public void visit(Eident id) {
		throw new Todo();
	}

	@Override
	public void visit(Ecall e) {
		throw new Todo();
	}

	@Override
	public void visit(Elist e) {
		throw new Todo();
	}

	@Override
	public void visit(Eget e) {
		throw new Todo();
	}

	// interpreting statements

	@Override
	public void visit(Seval s) {
		throw new Todo();
	}

	@Override
	public void visit(Sprint s) {
		throw new Todo();
	}

	@Override
	public void visit(Sblock s) {
		throw new Todo();
	}

	@Override
	public void visit(Sif s) {
		throw new Todo();
	}

	@Override
	public void visit(Sassign s) {
		throw new Todo();
	}

	@Override
	public void visit(Sreturn s) {
		throw new Todo();
	}

	@Override
	public void visit(Sfor s) {
		throw new Todo();
	}

	@Override
	public void visit(Sset s) {
		throw new Todo();
	}
}
