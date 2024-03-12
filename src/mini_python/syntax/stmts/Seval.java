package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

/**
 * Statement that evaluates to a value
 */
public class Seval extends Stmt {
    public final Expr e;

    public Seval(Expr e) {
        super();
        this.e = e;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}