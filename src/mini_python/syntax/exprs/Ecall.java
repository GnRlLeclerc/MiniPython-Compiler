package mini_python.syntax.exprs;

import java.util.LinkedList;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Ident;
import mini_python.syntax.Span;
import mini_python.syntax.visitors.Visitor;

/**
 * Call a function
 */
public class Ecall extends Expr {
    public final Ident f;
    public final LinkedList<Expr> l;

    public Ecall(Ident f, LinkedList<Expr> l) {
        super();
        this.f = f;
        this.l = l;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    protected Span buildSpan() {
        int start = this.f.loc.column;
        int length = this.f.loc.column + this.f.id.length() + 2; // By default, if no args, try to include naively the 2
                                                                 // parens.

        if (!l.isEmpty()) {
            Span last = l.getLast().getSpan();
            length = last.length - start + 1;
        }

        return new Span(this.f.loc, length);
    }
}