package mini_python;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.Stack;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Todo;
import mini_python.exception_handling.exceptions.RangeExpectedException;
import mini_python.exception_handling.exceptions.UndefinedIdentityException;
import mini_python.exception_handling.exceptions.WrongArgCountException;
import mini_python.syntax.Def;
import mini_python.syntax.Function;
import mini_python.syntax.Variable;
import mini_python.syntax.constants.Cbool;
import mini_python.syntax.constants.Cint;
import mini_python.syntax.constants.Cnone;
import mini_python.syntax.constants.Cstring;
import mini_python.syntax.exprs.Ebinop;
import mini_python.syntax.exprs.Ecall;
import mini_python.syntax.exprs.Ecst;
import mini_python.syntax.exprs.Eget;
import mini_python.syntax.exprs.Eident;
import mini_python.syntax.exprs.Elist;
import mini_python.syntax.exprs.Eunop;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.exprs_typed.TEbinop;
import mini_python.syntax.exprs_typed.TEcall;
import mini_python.syntax.exprs_typed.TEcst;
import mini_python.syntax.exprs_typed.TEget;
import mini_python.syntax.exprs_typed.TEident;
import mini_python.syntax.exprs_typed.TElist;
import mini_python.syntax.exprs_typed.TErange;
import mini_python.syntax.exprs_typed.TEunop;
import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.stmts.Sassign;
import mini_python.syntax.stmts.Sblock;
import mini_python.syntax.stmts.Seval;
import mini_python.syntax.stmts.Sfor;
import mini_python.syntax.stmts.Sif;
import mini_python.syntax.stmts.Sprint;
import mini_python.syntax.stmts.Sreturn;
import mini_python.syntax.stmts.Sset;
import mini_python.syntax.stmts.Stmt;
import mini_python.syntax.stmts_typed.TSassign;
import mini_python.syntax.stmts_typed.TSblock;
import mini_python.syntax.stmts_typed.TSeval;
import mini_python.syntax.stmts_typed.TSfor;
import mini_python.syntax.stmts_typed.TSif;
import mini_python.syntax.stmts_typed.TSprint;
import mini_python.syntax.stmts_typed.TSreturn;
import mini_python.syntax.stmts_typed.TSset;
import mini_python.syntax.stmts_typed.TStmt;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

/* The typer starts here */
class Typer implements Visitor {

	static public HashMap<String, Def> defs = new HashMap<>(); // For source code parsing on exception
	static public HashMap<String, Function> functions = new HashMap<>(); // For type checking, compilation, etc

	static {
		// Create the buitlin list function
		LinkedList<Variable> listParams = new LinkedList<>();
		Function list = new Function("list", listParams);
		listParams.add(Variable.mkVariable("l", 16));
		functions.put("list", list);

		// Create the buitlin len function
		LinkedList<Variable> lenParams = new LinkedList<>();
		Function len = new Function("len", lenParams);
		lenParams.add(Variable.mkVariable("l", 16));
		functions.put("len", len);

		// Create the buitlin range function
		LinkedList<Variable> rangeParams = new LinkedList<>();
		Function range = new Function("range", rangeParams);
		rangeParams.add(Variable.mkVariable("n", 16));
		functions.put("range", range);
	}

	// Store locally defined variables
	public HashMap<String, Variable> vars;

	// Store the return instructions of a function in order to determine its return
	// type
	public ArrayList<TSreturn> returns = new ArrayList<>();
	public TStmt currStmt;
	// Store the current function in which the typer is currently working in order
	// to remember in which stack frame
	// to allocate an offset for the new variables we might encounter.
	protected Function currentFunction;
	private TExpr currExpr;

	public Typer() {
		this.vars = new HashMap<>();
		this.currStmt = null;
		this.currExpr = null;
	}

	public void visit(Cnone c) {
		throw new Todo("Cnone");
	}

	public void visit(Cbool c) {
		throw new Todo("Cbool");
	}

	public void visit(Cstring c) {
		throw new Todo("Cstring");
	}

	public void visit(Cint c) {
		throw new Todo("Cint");
	}

	@Override
	public void visit(Ecst e) {
		this.currExpr = new TEcst(e.c);
	}

	@Override
	public void visit(Ebinop e) throws CompilationException {
		e.e1.accept(this);
		TExpr e1 = this.currExpr;
		e.e2.accept(this);
		TExpr e2 = this.currExpr;
		this.currExpr = new TEbinop(e.op, e1, e2);
	}

	@Override
	public void visit(Eunop e) throws CompilationException {
		e.e.accept(this);
		TExpr e1 = this.currExpr;
		this.currExpr = new TEunop(e.op, e1);
	}

	@Override
	public void visit(Eident id) throws CompilationException {
		Variable v = this.vars.get(id.x.id);
		if (v == null) {
			throw new UndefinedIdentityException(id.x.id, "value", id.x.loc);
		}
		this.currExpr = new TEident(v);
	}

	@Override
	public void visit(Ecall e) throws CompilationException {
		Function f = functions.get(e.f.id);
		if (f == null) {
			throw new UndefinedIdentityException(e.f.id, "function", e.f.loc);
		}
		if (f.params.size() != e.l.size()) {
			Def sourceDef = defs.get(e.f.id);
			throw new WrongArgCountException(e.f.loc, sourceDef, e.l.size());
		}
		if (f.name.equals("list")) {
			Expr firstCall = e.l.getFirst();
			if (!(firstCall instanceof Ecall) || !((Ecall) firstCall).f.id.equals("range")) {
				throw new RangeExpectedException(e.f.loc);
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
	public void visit(Elist e) throws CompilationException {
		LinkedList<TExpr> l = new LinkedList<>();
		for (Expr expr : e.l) {
			expr.accept(this);
			l.add(this.currExpr);
		}
		this.currExpr = new TElist(l);
	}

	@Override
	public void visit(Eget e) throws CompilationException {
		e.e1.accept(this);
		TExpr e1 = this.currExpr;
		e.e2.accept(this);
		TExpr e2 = this.currExpr;
		this.currExpr = new TEget(e1, e2);
	}

	@Override
	public void visit(Seval s) throws CompilationException {
		s.e.accept(this);
		this.currStmt = new TSeval(this.currExpr);
	}

	@Override
	public void visit(Sprint s) throws CompilationException {
		s.e.accept(this);
		this.currStmt = new TSprint(this.currExpr);
	}

	@Override
	public void visit(Sblock s) throws CompilationException {
		TSblock tsblock = new TSblock();
		for (Stmt stmt : s.l) {
			stmt.accept(this);
			tsblock.l.add(this.currStmt);
		}
		this.currStmt = tsblock;
	}

	@Override
	public void visit(Sif s) throws CompilationException {
		s.s1.accept(this);
		TStmt s1 = this.currStmt;
		s.s2.accept(this);
		TStmt s2 = this.currStmt;
		s.e.accept(this);
		this.currStmt = new TSif(this.currExpr, s1, s2);
	}

	@Override
	public void visit(Sassign s) throws CompilationException {
		s.e.accept(this);

		// Reuse or create the variable used to contain the assignment output
		Variable v = this.getOrCreateVar(s.x.id);
		v.type = this.currExpr.getType(); // Change the variable type to match the assignment

		Stack<TExpr> toVisit = new Stack<>();
		toVisit.push(this.currExpr);
		TEident variableEIdent = null;
		boolean onlyOne = true;
		while (!toVisit.isEmpty()) {
			TExpr e = toVisit.pop();
			// switch according to type of TExpr
			if (e instanceof TEident) {
				TEident eident = (TEident) e;
				if (eident.x.uid == v.uid && variableEIdent == null) {
					variableEIdent = eident;
					break;
				} else if (eident.x.uid == v.uid) {
					onlyOne = false;
					break;
				}
				break;
			} else if (e instanceof TEbinop) {
				TEbinop ebinop = (TEbinop) e;
				toVisit.push(ebinop.e1);
				toVisit.push(ebinop.e2);
			} else if (e instanceof TEunop) {
				TEunop eunop = (TEunop) e;
				toVisit.push(eunop.e);
			} else if (e instanceof TEcall) {
				TEcall ecall = (TEcall) e;
				for (TExpr expr : ecall.l) {
					toVisit.push(expr);
				}
			} else if (e instanceof TElist) {
				TElist elist = (TElist) e;
				for (TExpr expr : elist.l) {
					toVisit.push(expr);
				}
			} else if (e instanceof TEget) {
				TEget eget = (TEget) e;
				toVisit.push(eget.e1);
				toVisit.push(eget.e2);
			} else if (e instanceof TErange) {
				TErange erange = (TErange) e;
				toVisit.push(erange.e);
			}
		}
		if (variableEIdent != null && onlyOne) {
			// cannot use temporary as a variable can point to a value referenced
			// elsewhere
			variableEIdent.reassignement = true;
		}

		this.currStmt = new TSassign(v, this.currExpr);
	}

	@Override
	public void visit(Sreturn s) throws CompilationException {
		s.e.accept(this);
		TSreturn tsreturn = new TSreturn(this.currExpr);

		this.returns.add(tsreturn); // Add the return statement to the list of returns
		this.currStmt = tsreturn;
	}

	@Override
	public void visit(Sfor s) throws CompilationException {
		// Reuse or create the variable used to contain the loop output
		Variable v = this.getOrCreateVar(s.x.id);
		s.s.accept(this);
		s.e.accept(this);

		this.currStmt = new TSfor(v, this.currExpr, this.currStmt);
	}

	@Override
	public void visit(Sset s) throws CompilationException {
		s.e1.accept(this);
		TExpr e1 = this.currExpr;
		s.e2.accept(this);
		TExpr e2 = this.currExpr;
		s.e3.accept(this);
		TExpr e3 = this.currExpr;
		this.currStmt = new TSset(e1, e2, e3);
	}

	/**
	 * Helper function to compute the output type of a function from the stored
	 * return statements.
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

		// If there are multiple types, return dynamic. Even if types BOOL and INT64 are
		// returned, we do not coerce them.
		if (types.size() > 1) {
			return Type.DYNAMIC;
		}

		// By default (0 types), return NoneType
		return Type.NONETYPE;
	}

	/**
	 * Sets the "returnType" member of all current TSreturn statements to the given
	 * type.
	 * Call this function after having finished to parse a function, and determined
	 * its actual return type.
	 */
	public void setReturnTypes(Type type) {
		for (TSreturn tsreturn : this.returns) {
			tsreturn.returnType = type;
		}
	}

	/**
	 * Helper: given a variable name, get it from the current scope or create it.
	 * This will help avoid allocating new space on the stack when reusing a
	 * variable name for other assignments
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
