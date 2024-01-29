package mini_python;

import java.util.HashMap;
import java.util.HashSet;

// the following exception is used whenever you have to implement something
class Todo extends Error {
    private static final long serialVersionUID = 1L;

    Todo() {
        super("TODO");
    }
}

/* The typer starts here */
class Typer implements TVisitor {

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

    // local variables
    HashSet<String> vars;

    Typer() {
        this.vars = new HashSet<String>();
    }

    // functions definitions (functions are global, hence `static`)
    static HashMap<String, TDef> functions = new HashMap<String, TDef>();

    static TFile tFile = new TFile();

    // interpreting expressions

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
    public void visit(TEident id) {
        throw new Todo();
    }

    @Override
    public void visit(TEcall e) {
        throw new Todo();
    }

    @Override
    public void visit(TErange e) {
        throw new Todo();
    }

    @Override
    public void visit(TElist e) {
        throw new Todo();
    }

    @Override
    public void visit(TEget e) {
        throw new Todo();
    }

    // interpreting statements

    @Override
    public void visit(TSeval s) {
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
    public void visit(TSif s) {
        throw new Todo();
    }

    @Override
    public void visit(TSassign s) {
        throw new Todo();
    }

    @Override
    public void visit(TSreturn s) {
        throw new Todo();
    }

    @Override
    public void visit(TSfor s) {
        throw new Todo();
    }

    @Override
    public void visit(TSset s) {
        throw new Todo();
    }
}
