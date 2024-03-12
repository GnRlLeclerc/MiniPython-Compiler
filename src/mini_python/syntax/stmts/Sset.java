package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

/**
 * Set a value in a list element
 */
public class Sset extends Stmt {
    public final Expr e1;
    public final Expr e2;
    public final Expr e3;

    public Sset(Expr e1, Expr e2, Expr e3) {
        super();
        this.e1 = e1;
        this.e2 = e2;
        this.e3 = e3;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}