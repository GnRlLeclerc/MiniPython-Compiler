package mini_python.syntax.exprs;

import mini_python.syntax.Span;
import mini_python.syntax.constants.Constant;
import mini_python.syntax.visitors.Visitor;

/**
 * Constant expressions
 */
public class Ecst extends Expr {
    public final Constant c;

    public Ecst(Constant c) {
        super();
        this.c = c;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public Span buildSpan() {
        return new Span(this.c.location, this.c.length());
    }
}