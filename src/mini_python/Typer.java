package mini_python;

import java.io.Serial;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import mini_python.typing.Type;

// the following exception is used whenever you have to implement something
class Todo extends Error {
	@Serial
	private static final long serialVersionUID = 1L;

	Todo(String message) {
		super("TODO: " + message);
	}

	Todo() {
		super("TODO");
	}
}

/* The typer starts here */
class Typer implements Visitor {

	static public HashMap<String, Function> functions = new HashMap<>();

	static {
		// Create the buitlin list function
		LinkedList<Variable> listParams = new LinkedList<>();
		Function list = new Function("list", listParams);
		listParams.add(Variable.mkVariable("l", list.getStackFrameOffset()));
		functions.put("list", list);

		// Create the buitlin len function
		LinkedList<Variable> lenParams = new LinkedList<>();
		Function len = new Function("len", lenParams);
		lenParams.add(Variable.mkVariable("l", len.getStackFrameOffset()));
		functions.put("len", len);

		// Create the buitlin range function
		LinkedList<Variable> rangeParams = new LinkedList<>();
		Function range = new Function("range", rangeParams);
		rangeParams.add(Variable.mkVariable("n", range.getStackFrameOffset()));
		functions.put("range", range);
	}

	// Store locally defined variables
	public HashMap<String, Variable> vars;

	// Store the return instructions of a function in order to determine its return type
	public ArrayList<TSreturn> returns = new ArrayList<>();
	public TStmt currStmt;
	// Store the current function in which the typer is currently working in order to remember in which stack frame
	// to allocate an offset for the new variables we might encounter.
	protected Function currentFunction;
	private TExpr currExpr;

	public Typer() {
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

		// Reuse or create the variable used to contain the assignment output
		Variable v = this.getOrCreateVar(s.x.id);
		v.type = this.currExpr.getType(); // Change the variable type to match the assignment

		this.currStmt = new TSassign(v, this.currExpr);
	}

	@Override
	public void visit(Sreturn s) {
		s.e.accept(this);
		TSreturn tsreturn = new TSreturn(this.currExpr);

		this.returns.add(tsreturn); // Add the return statement to the list of returns
		this.currStmt = tsreturn;
	}

	@Override
	public void visit(Sfor s) {
		// Reuse or create the variable used to contain the loop output
		Variable v = this.getOrCreateVar(s.x.id);
		s.s.accept(this);
		s.e.accept(this);
		
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

	/**
	 * Helper function to compute the output type of a function from the stored return statements.
	 * This function never returns null and defaults to NoneType
	 */
	public Type currentReturnType() {

		Set<Type> types = new HashSet<>();

		for (TSreturn tsreturn : this.returns) {
			types.add(tsreturn.returnType);
		}

		// If there is only one type, return it
		if (types.size() == 1) {
			return types.iterator().next();
		}

		// If there are multiple types, return dynamic. Even if types BOOL and INT64 are returned, we do not coerce them.
		if (types.size() > 1) {
			return Type.DYNAMIC;
		}

		// By default (0 types), return NoneType
		return Type.NONETYPE;
	}

	/**
	 * Sets the "returnType" member of all current TSreturn statements to the given type.
	 * Call this function after having finished to parse a function, and determined its actual return type.
	 */
	public void setReturnTypes(Type type) {
		for (TSreturn tsreturn : this.returns) {
			tsreturn.returnType = type;
		}
	}

	/**
	 * Helper: given a variable name, get it from the current scope or create it.
	 * This will help avoid allocating new space on the stack when reusing a variable name for other assignments
	 */
	public Variable getOrCreateVar(String id) {
		Variable v;
		if (!this.vars.containsKey(id)) {
			v = Variable.mkVariable(id, this.currentFunction.getStackFrameOffset());
			this.vars.put(id, v);
		} else {
			v = this.vars.get(id);
		}
		return v;
	}
}
