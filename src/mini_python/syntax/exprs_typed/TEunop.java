package mini_python.syntax.exprs_typed;

import mini_python.exception_handling.exceptions.InvalidUnopTypeException;
import mini_python.syntax.Span;
import mini_python.syntax.operations.Unop;
import mini_python.syntax.visitors.TVisitor;

/**
 * Operations with arity 1
 */
public class TEunop extends TExpr {
    public final Unop op;
    public final TExpr e;

    public TEunop(Unop op, TExpr e, Span span) throws InvalidUnopTypeException {
        super(op.coerce(e.getType()), true);
        this.op = op;
        this.e = e;

        if (this.getType() == null) {
            throw new InvalidUnopTypeException(span, op, e.getType());
        }
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
