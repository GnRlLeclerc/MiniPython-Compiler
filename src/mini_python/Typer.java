package mini_python;

import java.io.Serial;
import java.util.HashMap;
import java.util.LinkedList;

// the following exception is used whenever you have to implement something
class Todo extends Error {
	@Serial
	private static final long serialVersionUID = 1L;

	Todo() {
		super("TODO");
	}
}

/* The typer starts here */
class Typer implements Visitor {

	static public HashMap<String, Function> functions = new HashMap<>();

	static {
		LinkedList<Variable> listParams = new LinkedList<>();
		listParams.add(Variable.mkVariable("l"));
		functions.put("list", new Function("list", listParams));
		LinkedList<Variable> lenParams = new LinkedList<>();
		lenParams.add(Variable.mkVariable("l"));
		functions.put("len", new Function("len", lenParams));
		LinkedList<Variable> rangeParams = new LinkedList<>();
		rangeParams.add(Variable.mkVariable("n"));
		functions.put("range", new Function("range", rangeParams));
	}

	public HashMap<String, Variable> vars;
	public TStmt currStmt;
	private TExpr currExpr;

	Typer() {
		this.vars = new HashMap<>();
		this.currStmt = null;
		this.currExpr = null;
	}

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

	@Override
	public void visit(Ecst e) {
		this.currExpr = new TEcst(e.c);
	}

	@Override
	public void visit(Ebinop e) {
		e.e1.accept(this);
		TExpr e1 = this.currExpr;
		e.e2.accept(this);
		TExpr e2 = this.currExpr;
		this.currExpr = new TEbinop(e.op, e1, e2);
	}

	@Override
	public void visit(Eunop e) {
		e.e.accept(this);
		TExpr e1 = this.currExpr;
		this.currExpr = new TEunop(e.op, e1);
	}

	@Override
	public void visit(Eident id) {
		Variable v = this.vars.get(id.x.id);
		if (v == null) {
			throw new Error("undefined variable: " + id.x.id + " at " + id.x.loc);
		}
		this.currExpr = new TEident(v);
	}

	@Override
	public void visit(Ecall e) {
		Function f = functions.get(e.f.id);
		if (f == null) {
			throw new Error("undefined function: " + e.f.id + " at " + e.f.loc);
		}
		if (f.params.size() != e.l.size()) {
			throw new Error("function " + e.f.id + " expects " + f.params.size() + " arguments, but " + e.l.size()
					+ " were given at " + e.f.loc);
		}
		if (f.name.equals("list")) {
			Expr firstCall = e.l.getFirst();
			if (!(firstCall instanceof Ecall) || !((Ecall) firstCall).f.id.equals("range")) {
				throw new Error("list expects a range call at " + e.f.loc);
			}
		}
		if (f.name.equals("range")) {
			e.l.getFirst().accept(this);
			TExpr e1 = this.currExpr;
			this.currExpr = new TErange(e1);
			return;
		}
		LinkedList<TExpr> l = new LinkedList<>();
		for (Expr expr : e.l) {
			expr.accept(this);
			l.add(this.currExpr);
		}
		this.currExpr = new TEcall(f, l);
	}

	@Override
	public void visit(Elist e) {
		LinkedList<TExpr> l = new LinkedList<>();
		for (Expr expr : e.l) {
			expr.accept(this);
			l.add(this.currExpr);
		}
		this.currExpr = new TElist(l);
	}

	@Override
	public void visit(Eget e) {
		e.e1.accept(this);
		TExpr e1 = this.currExpr;
		e.e2.accept(this);
		TExpr e2 = this.currExpr;
		this.currExpr = new TEget(e1, e2);
	}

	@Override
	public void visit(Seval s) {
		s.e.accept(this);
		this.currStmt = new TSeval(this.currExpr);
	}

	@Override
	public void visit(Sprint s) {
		s.e.accept(this);
		this.currStmt = new TSprint(this.currExpr);
	}

	@Override
	public void visit(Sblock s) {
		TSblock tsblock = new TSblock();
		for (Stmt stmt : s.l) {
			stmt.accept(this);
			tsblock.l.add(this.currStmt);
		}
		this.currStmt = tsblock;
	}

	@Override
	public void visit(Sif s) {
		s.s1.accept(this);
		TStmt s1 = this.currStmt;
		s.s2.accept(this);
		TStmt s2 = this.currStmt;
		s.e.accept(this);
		this.currStmt = new TSif(this.currExpr, s1, s2);
	}

	@Override
	public void visit(Sassign s) {
		s.e.accept(this);
		Variable v = Variable.mkVariable(s.x.id);
		this.vars.put(s.x.id, v);
		this.currStmt = new TSassign(v, this.currExpr);
	}

	@Override
	public void visit(Sreturn s) {
		s.e.accept(this);
		this.currStmt = new TSreturn(this.currExpr);
	}

	@Override
	public void visit(Sfor s) {
		Variable v = Variable.mkVariable(s.x.id);
		this.vars.put(s.x.id, v);
		s.e.accept(this);
		s.s.accept(this);
		this.currStmt = new TSfor(v, this.currExpr, this.currStmt);
	}

	@Override
	public void visit(Sset s) {
		s.e1.accept(this);
		TExpr e1 = this.currExpr;
		s.e2.accept(this);
		TExpr e2 = this.currExpr;
		s.e3.accept(this);
		TExpr e3 = this.currExpr;
		this.currStmt = new TSset(e1, e2, e3);
	}
}
