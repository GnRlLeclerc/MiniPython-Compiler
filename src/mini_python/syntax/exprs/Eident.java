package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Ident;
import mini_python.syntax.Span;
import mini_python.syntax.visitors.Visitor;

/**
 * Access a variable value
 */
public class Eident extends Expr {
    public final Ident x;

    public Eident(Ident x) {
        super();
        this.x = x;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }

    @Override
    public Span buildSpan() {
        return new Span(this.x.loc, this.x.id.length());
    }
}
