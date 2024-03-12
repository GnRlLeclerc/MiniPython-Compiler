package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Span;
import mini_python.syntax.visitors.Visitor;

public abstract class Expr {

    abstract public void accept(Visitor v) throws CompilationException;

    /**
     * Get the source code spanning of this particular expression.
     * NOTE: we assume that the span fits on one line ! We do not handle multiline
     * source
     * code error messages.
     */
    abstract public Span getSpan();
}