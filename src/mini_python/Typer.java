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
    static {
        functions.put("list", null);
        functions.put("len", null);
        functions.put("range", null);
    }

    // local variables
    public HashMap<String, Variable> vars;
    public TStmt currStmt;
    private TExpr currExpr;

    Typer() {
        this.vars = new HashMap<>();
        this.currStmt = null;
        this.currExpr = null;
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
        TEcst tEcst = new TEcst(e.c);
        this.currExpr = tEcst;
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
        s.e.accept(this);
        TSprint tsprint = new TSprint(this.currExpr);
        this.currStmt = tsprint;
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
