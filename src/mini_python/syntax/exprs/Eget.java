package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Span;
import mini_python.syntax.visitors.Visitor;

/**
 * Access a value from a list
 */
public class Eget extends Expr {
    public final Expr e1;
    public final Expr e2;

    public Eget(Expr e1, Expr e2) {
        super();
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    public Span buildSpan() {
        return this.e1.getSpan(); // Just return the list identity
    }
}