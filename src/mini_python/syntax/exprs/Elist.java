package mini_python.syntax.exprs;

import java.util.LinkedList;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Todo;
import mini_python.syntax.Span;
import mini_python.syntax.visitors.Visitor;

/**
 * Create a list from a list of expressions
 */
public class Elist extends Expr {
    public final LinkedList<Expr> l;

    public Elist(LinkedList<Expr> l) {
        super();
        this.l = l;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    public Span getSpan() {
        throw new Todo("Elist.getSpan");
    }
}
