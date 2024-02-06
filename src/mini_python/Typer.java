package mini_python;

import java.util.HashMap;
import java.util.LinkedList;

// the following exception is used whenever you have to implement something
class Todo extends Error {
    private static final long serialVersionUID = 1L;

    Todo() {
        super("TODO");
    }
}

/* The typer starts here */
class Typer implements Visitor {

    static public HashMap<String, Function> functions = new HashMap<>();
    {
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
        TEcst tEcst = new TEcst(e.c);
        this.currExpr = tEcst;
    }

    @Override
    public void visit(Ebinop e) {
        e.e1.accept(this);
        TExpr e1 = this.currExpr;
        e.e2.accept(this);
        TExpr e2 = this.currExpr;
        TEbinop tebinop = new TEbinop(e.op, e1, e2);
        this.currExpr = tebinop;
    }

    @Override
    public void visit(Eunop e) {
        e.e.accept(this);
        TExpr e1 = this.currExpr;
        TEunop teunop = new TEunop(e.op, e1);
        this.currExpr = teunop;
    }

    @Override
    public void visit(Eident id) {
        Variable v = this.vars.get(id.x.id);
        if (v == null) {
            throw new Error("undefined variable: " + id.x.id + " at " + id.x.loc);
        }
        TEident teident = new TEident(v);
        this.currExpr = teident;
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
            Expr firstCall = e.l.get(0);
            if (!(firstCall instanceof Ecall) || !((Ecall) firstCall).f.id.equals("range")) {
                throw new Error("list expects a range call at " + e.f.loc);
            }
        }
        if (f.name.equals("range")) {
            e.l.get(0).accept(this);
            TExpr e1 = this.currExpr;
            TErange terange = new TErange(e1);
            this.currExpr = terange;
            return;
        }
        LinkedList<TExpr> l = new LinkedList<>();
        for (Expr expr : e.l) {
            expr.accept(this);
            l.add(this.currExpr);
        }
        TEcall tecall = new TEcall(f, l);
        this.currExpr = tecall;
    }

    @Override
    public void visit(Elist e) {
        LinkedList<TExpr> l = new LinkedList<>();
        for (Expr expr : e.l) {
            expr.accept(this);
            l.add(this.currExpr);
        }
        TElist teList = new TElist(l);
        this.currExpr = teList;
    }

    @Override
    public void visit(Eget e) {
        e.e1.accept(this);
        TExpr e1 = this.currExpr;
        e.e2.accept(this);
        TExpr e2 = this.currExpr;
        TEget teget = new TEget(e1, e2);
        this.currExpr = teget;
    }

    @Override
    public void visit(Seval s) {
        s.e.accept(this);
        TSeval tseval = new TSeval(this.currExpr);
        this.currStmt = tseval;
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
        s.s1.accept(this);
        TStmt s1 = this.currStmt;
        s.s2.accept(this);
        TStmt s2 = this.currStmt;
        s.e.accept(this);
        TSif tsif = new TSif(this.currExpr, s1, s2);
        this.currStmt = tsif;
    }

    @Override
    public void visit(Sassign s) {
        s.e.accept(this);
        Variable v = Variable.mkVariable(s.x.id);
        this.vars.put(s.x.id, v);
        TSassign tsassign = new TSassign(v, this.currExpr);
        this.currStmt = tsassign;
    }

    @Override
    public void visit(Sreturn s) {
        s.e.accept(this);
        TSreturn tsreturn = new TSreturn(this.currExpr);
        this.currStmt = tsreturn;
    }

    @Override
    public void visit(Sfor s) {
        Variable v = Variable.mkVariable(s.x.id);
        this.vars.put(s.x.id, v);
        s.e.accept(this);
        s.s.accept(this);
        TSfor tsfor = new TSfor(v, this.currExpr, this.currStmt);
        this.currStmt = tsfor;
    }

    @Override
    public void visit(Sset s) {
        s.e1.accept(this);
        TExpr e1 = this.currExpr;
        s.e2.accept(this);
        TExpr e2 = this.currExpr;
        s.e3.accept(this);
        TExpr e3 = this.currExpr;
        TSset tsset = new TSset(e1, e2, e3);
        this.currStmt = tsset;
    }
}
