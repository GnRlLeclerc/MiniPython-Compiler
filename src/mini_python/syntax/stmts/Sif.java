package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

/**
 * If block.
 * If (e) { s1 } else { s2 }
 */
public class Sif extends Stmt {
    public final Expr e;
    public final Stmt s1;
    public final Stmt s2;

    public Sif(Expr e, Stmt s1, Stmt s2) {
        super();
        this.e = e;
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}
