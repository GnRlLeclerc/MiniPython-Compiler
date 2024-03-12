package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Ident;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

/**
 * For loop
 * for (x in e) { s }
 */
public class Sfor extends Stmt {
    public final Ident x;
    public final Expr e;
    public final Stmt s;

    public Sfor(Ident x, Expr e, Stmt s) {
        super();
        this.x = x;
        this.e = e;
        this.s = s;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}