package mini_python;

import mini_python.typing.Type;

import java.util.LinkedList;

/* Abstract Syntax of Mini-Python */

/* Parsed trees.
   This is the output of the parser and the input of the type checker. */

enum Unop {
	Uneg, Unot;

	/**
	 * Given a type, build the resulting type of the operation.
	 * Returns null if the operation is invalid
	 */
	Type coerce(Type type) {
		if (type == Type.DYNAMIC) {
			return Type.DYNAMIC;
		}

		switch (this) {
			case Uneg:
				if (type == Type.INT64 || type == Type.BOOL) {
					return Type.INT64;
				}
			case Unot:
				return Type.BOOL; // All types can be coerced to bool
		}

		return null; // Default output: coercion failed
	}
}

enum Binop {
	Badd, Bsub, Bmul, Bdiv, Bmod,
	Beq, Bneq, Blt, Ble, Bgt, Bge,
	Band, Bor;

	/**
	 * Given two operand types, return the output type
	 * Returns null if the operation is invalid
	 */
	Type coerce(Type type1, Type type2) {

		// Basic operations or comparisons
		if (this == Band || this == Bor || this == Beq || this == Bneq) {
			return Type.BOOL; // All types can be coerced to bool, and these operations return a bool
		}

		// Ordering operations : the types must be coerccible together
		Type type = type1.coerce(type2);
		if (type != null && (this == Blt || this == Ble || this == Bgt || this == Bge || this == Badd || this == Bsub)) {
			return type;
		}

		// NOTE: we do not fully implement all python operations. The following can only be done with int-type operands
		if ((type == Type.INT64 || type == Type.BOOL) && (this == Bmul || this == Bdiv || this == Bmod)) {
			return Type.INT64;
		}

		return null; // Default output: coercion failed
	}
}

/* unary and binary operators */

interface Visitor {
	void visit(Cnone c);

	void visit(Cbool c);

	void visit(Cstring c);

	void visit(Cint c);

	void visit(Ecst e);

	void visit(Ebinop e);

	void visit(Eunop e);

	void visit(Eident e);

	void visit(Ecall e);

	void visit(Eget e);

	void visit(Elist e);

	void visit(Sif s);

	void visit(Sreturn s);

	void visit(Sassign s);

	void visit(Sprint s);

	void visit(Sblock s);

	void visit(Sfor s);

	void visit(Seval s);

	void visit(Sset s);
}

interface TVisitor {
	void visit(Cnone c);

	void visit(Cbool c);

	void visit(Cstring c);

	void visit(Cint c);

	void visit(TEcst e);

	void visit(TEbinop e);

	void visit(TEunop e);

	void visit(TEident e);

	void visit(TEcall e);

	void visit(TEget e);

	void visit(TElist e);

	void visit(TErange e);

	void visit(TSif s);

	void visit(TSreturn s);

	void visit(TSassign s);

	void visit(TSprint s);

	void visit(TSblock s);

	void visit(TSfor s);

	void visit(TSeval s);

	void visit(TSset s);
}

/**
 * Code location in the source file
 */
class Location {
	final int line;
	final int column;

	Location(int line, int column) {
		this.line = line + 1;
		this.column = column;
	}

	@Override
	public String toString() {
		return this.line + ":" + this.column + ":";
	}
}

/**
 * Function or variable identifier
 */
class Ident {
	final String id;
	final Location loc;

	Ident(String id) {
		this.id = id;
		this.loc = null;
	}

	Ident(String id, Location loc) {
		this.id = id;
		this.loc = loc;
	}
}

/**
 * Constant (string, bool, int...)
 */
abstract class Constant {
	static final Cnone None = new Cnone();

	abstract void accept(Visitor v);

	abstract void accept(TVisitor v);

	abstract Type getType();
}

class Cnone extends Constant {
	@Override
	void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}

	@Override
	Type getType() {
		return Type.NONETYPE;
	}
}

class Cbool extends Constant {
	final boolean b;

	Cbool(boolean b) {
		this.b = b;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}

	@Override
	Type getType() {
		return Type.BOOL;
	}
}

/* expressions */

class Cstring extends Constant {
	final String s;

	Cstring(String s) {
		this.s = s;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}

	@Override
	Type getType() {
		return Type.STRING;
	}
}

class Cint extends Constant {
	final long i; // Python has arbitrary-precision integers; we simplify here

	Cint(long i) {
		this.i = i;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}

	@Override
	Type getType() {
		return Type.INT64;
	}
}

abstract class Expr {
	abstract void accept(Visitor v);
}

/**
 * Constant expressions
 */
class Ecst extends Expr {
	final Constant c;

	Ecst(Constant c) {
		this.c = c;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Operations with arity 2
 */
class Ebinop extends Expr {
	final Binop op;
	final Expr e1, e2;

	Ebinop(Binop op, Expr e1, Expr e2) {
		super();
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Operations with arity 1
 */
class Eunop extends Expr {
	final Unop op;
	final Expr e;

	Eunop(Unop op, Expr e) {
		super();
		this.op = op;
		this.e = e;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Access a variable value
 */
class Eident extends Expr {
	final Ident x;

	Eident(Ident x) {
		super();
		this.x = x;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Access a value from a list
 */
class Eget extends Expr {
	final Expr e1, e2;

	Eget(Expr e1, Expr e2) {
		super();
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}


/**
 * Call a function
 */
class Ecall extends Expr {
	final Ident f;
	final LinkedList<Expr> l;

	Ecall(Ident f, LinkedList<Expr> l) {
		super();
		this.f = f;
		this.l = l;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Create a list from a list of expressions
 */
class Elist extends Expr {
	final LinkedList<Expr> l;

	Elist(LinkedList<Expr> l) {
		super();
		this.l = l;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

abstract class Stmt {
	abstract void accept(Visitor v);
}

/**
 * If block.
 * If (e) { s1 } else { s2 }
 */
class Sif extends Stmt {
	final Expr e;
	final Stmt s1, s2;

	Sif(Expr e, Stmt s1, Stmt s2) {
		super();
		this.e = e;
		this.s1 = s1;
		this.s2 = s2;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

class Sreturn extends Stmt {
	final Expr e;

	Sreturn(Expr e) {
		super();
		this.e = e;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Assign a value to a variable
 */
class Sassign extends Stmt {
	final Ident x;
	final Expr e;

	Sassign(Ident x, Expr e) {
		super();
		this.x = x;
		this.e = e;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Print a value to standard output
 */
class Sprint extends Stmt {
	final Expr e;

	Sprint(Expr e) {
		super();
		this.e = e;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Block of statements (scope)
 */
class Sblock extends Stmt {
	final LinkedList<Stmt> l;

	Sblock() {
		this.l = new LinkedList<Stmt>();
	}

	Sblock(LinkedList<Stmt> l) {
		super();
		this.l = l;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * For loop
 * for (x in e) { s }
 */
class Sfor extends Stmt {
	final Ident x;
	final Expr e;
	final Stmt s;

	Sfor(Ident x, Expr e, Stmt s) {
		super();
		this.x = x;
		this.e = e;
		this.s = s;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}


/**
 * Statement that evaluates to a value
 */
class Seval extends Stmt {
	final Expr e;

	Seval(Expr e) {
		super();
		this.e = e;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/**
 * Set a value in a list element
 */
class Sset extends Stmt {
	final Expr e1, e2, e3;

	Sset(Expr e1, Expr e2, Expr e3) {
		super();
		this.e1 = e1;
		this.e2 = e2;
		this.e3 = e3;
	}

	@Override
	void accept(Visitor v) {
		v.visit(this);
	}
}

/*
 * visitor for the parsed trees
 * (feel free to modify it for your needs)
 */

/**
 * Function definition
 */
class Def {
	final Ident f;
	final LinkedList<Ident> l; // formal parameters
	final Stmt s;

	Def(Ident f, LinkedList<Ident> l, Stmt s) {
		super();
		this.f = f;
		this.l = l;
		this.s = s;
	}
}

/*
 * Typed trees.
 *
 * This is the output of the type checker and the input of the code
 * generation.
 *
 * In the typed trees, identifiers (objects of class `Ident`) are
 * now turned into objects of class `Variable` or `Function`.
 *
 * There is also a new class `TErange` for the Python expression
 * `list(range(e))`.
 */

class File {
	final LinkedList<Def> l;
	final Stmt s; // a block of global statements

	File(LinkedList<Def> l, Stmt s) {
		super();
		this.l = l;
		this.s = s;
	}
}

/*
 * In the typed trees, all the occurrences of the same variable
 * point to a single object of the following class.
 */
class Variable {
	private static int id = 0;
	final String name; // for debugging purposes
	public Type type; // The type of a variable can be dynamically set, and can change on reassign.
	int uid; // unique id, for debugging purposes
	int ofs; // position wrt %rbp

	private Variable(String name, int uid, Type type) {
		this.name = name;
		this.uid = uid;
		this.ofs = -1; // will be set later, during code generation

		// Default to NoneType
		if (type == null) {
			type = Type.NONETYPE;
		}
		this.type = type;
	}

	/**
	 * Create a new variable with default type NoneType
	 */
	static Variable mkVariable(String name) {
		return new Variable(name, id++, Type.DYNAMIC);
	}

	/**
	 * Create a new variable with a given type
	 */
	static Variable mkVariable(String name, Type type) {
		return new Variable(name, id++, type);
	}
}

/*
 * Similarly, all the occurrences of a given function all point
 * to a single object of the following class.
 */
class Function {
	final String name;
	final LinkedList<Variable> params;

	// The return type of a function will be determined after having parsed all of its return statements.
	// If all types can be cast to one, a static type will be used. Else, the return type will be dynamic.
	public Type returnType;

	Function(String name, LinkedList<Variable> params) {
		this.name = name;
		this.params = params;
		this.returnType = Type.NONETYPE; // By default
	}
}

/**
 * Abstract base class for typed expressions.
 * As we use static typing, every expression has a type.
 */
abstract class TExpr {

	protected final Type type;

	TExpr(Type type) {
		assert type != null;
		this.type = type;
	}

	abstract void accept(TVisitor v);

	Type getType() {
		return type;
	}
}

/**
 * Constant expressions
 */
class TEcst extends TExpr {
	final Constant c;

	TEcst(Constant c) {
		super(c.getType());
		this.c = c;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Operations with arity 2
 */
class TEbinop extends TExpr {
	final Binop op;
	final TExpr e1, e2;

	TEbinop(Binop op, TExpr e1, TExpr e2) {
		super(op.coerce(e1.getType(), e2.getType()));
		this.op = op;
		this.e1 = e1;
		this.e2 = e2;

		if (this.getType() == null) {
			throw new Error("Invalid types " + e1.getType() + " and " + e2.getType() + " for operation " + op);
		}
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Operations with arity 1
 */
class TEunop extends TExpr {
	final Unop op;
	final TExpr e;

	TEunop(Unop op, TExpr e) {
		super(op.coerce(e.getType()));
		this.op = op;
		this.e = e;

		if (this.getType() == null) {
			throw new Error("Invalid type " + e.getType() + " for operand " + op);
		}
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Access a variable value
 */
class TEident extends TExpr {
	final Variable x;

	TEident(Variable x) {
		super(x.type);
		this.x = x;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Get a value from a list.
 * e1 is the list expression or variable, and e2 is the index.
 */
class TEget extends TExpr {
	final TExpr e1, e2;

	TEget(TExpr e1, TExpr e2) {
		super(Type.DYNAMIC); // List can contain multiple elements
		this.e1 = e1;
		this.e2 = e2;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Call a function
 */
class TEcall extends TExpr {
	final Function f;
	final LinkedList<TExpr> l;

	TEcall(Function f, LinkedList<TExpr> l) {
		super(f.returnType);
		this.f = f;
		this.l = l;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Create a list from a list of expressions
 */
class TElist extends TExpr {
	final LinkedList<TExpr> l;

	TElist(LinkedList<TExpr> l) {
		super(Type.LIST);
		this.l = l;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Create a list from a range
 */
class TErange extends TExpr {
	final TExpr e;

	TErange(TExpr e) {
		super(Type.RANGE);
		this.e = e;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

abstract class TStmt {
	abstract void accept(TVisitor v);
}

/**
 * If block.
 * If (e) { s1 } else { s2 }
 */
class TSif extends TStmt {
	final TExpr e;
	final TStmt s1, s2;

	TSif(TExpr e, TStmt s1, TStmt s2) {
		super();
		this.e = e;
		this.s1 = s1;
		this.s2 = s2;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

class TSreturn extends TStmt {
	final TExpr e;

	// The default return type is the same as the expression.
	// However, when a function can return multiple types, we use the "dynamic" type.
	// In this case, the TSreturn statement will have "dynamic" set as its return type, and will know to convert
	// the original statement type to the dynamic one.
	public Type returnType;

	TSreturn(TExpr e) {
		super();
		this.returnType = e.getType();
		this.e = e;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Assign a value to a variable
 */
class TSassign extends TStmt {
	final Variable x;
	final TExpr e;

	TSassign(Variable x, TExpr e) {
		super();
		this.x = x;
		this.e = e;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Print a value to standard output
 */
class TSprint extends TStmt {
	final TExpr e;

	TSprint(TExpr e) {
		super();
		this.e = e;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Block of statements (scope)
 */
class TSblock extends TStmt {
	final LinkedList<TStmt> l;

	TSblock() {
		this.l = new LinkedList<TStmt>();
	}

	TSblock(LinkedList<TStmt> l) {
		super();
		this.l = l;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * For loop
 * for (x in e) { s }
 */
class TSfor extends TStmt {
	final Variable x;
	final TExpr e;
	final TStmt s;

	TSfor(Variable x, TExpr e, TStmt s) {
		super();
		this.x = x;
		this.e = e;
		this.s = s;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Statement that evaluates to a value
 */
class TSeval extends TStmt {
	final TExpr e;

	TSeval(TExpr e) {
		super();
		this.e = e;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Assign a value to a list element
 */

class TSset extends TStmt {
	final TExpr e1, e2, e3;

	TSset(TExpr e1, TExpr e2, TExpr e3) {
		super();
		this.e1 = e1;
		/* function definition and file */
		this.e2 = e2;
		this.e3 = e3;
	}

	@Override
	void accept(TVisitor v) {
		v.visit(this);
	}
}

/**
 * Function definition
 */
class TDef {
	final Function f;
	final TStmt body;

	TDef(Function f, TStmt body) {
		super();
		this.f = f;
		this.body = body;
	}
}

/*
 * visitor for the typed trees
 * (feel free to modify it for your needs)
 */

class TFile {
	final LinkedList<TDef> l;
	// the block of global statements is now a `main` function

	TFile() {
		super();
		this.l = new LinkedList<>();
	}
}
