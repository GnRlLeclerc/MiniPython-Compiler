package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Span;
import mini_python.syntax.operations.Unop;
import mini_python.syntax.visitors.Visitor;

/**
 * Operations with arity 1
 */
public class Eunop extends Expr {
    public final Unop op;
    public final Expr e;

    public Eunop(Unop op, Expr e) {
        super();
        this.op = op;
        this.e = e;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    public Span getSpan() {
        return e.getSpan(); // Returning the main expression span is enough
    }

}
