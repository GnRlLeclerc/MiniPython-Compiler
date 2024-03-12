package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Span;
import mini_python.syntax.operations.Binop;
import mini_python.syntax.visitors.Visitor;

/**
 * Operations with arity 2
 */
public class Ebinop extends Expr {
    public final Binop op;
    public final Expr e1, e2;

    public Ebinop(Binop op, Expr e1, Expr e2) {
        super();
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    public Span getSpan() {
        Span second = e2.getSpan();
        return new Span(e1.getSpan().start, second.start.column + second.length);
    }
}
