package mini_python.syntax.exprs_typed;

import mini_python.exception_handling.exceptions.InvalidBinopTypesException;
import mini_python.syntax.operations.Binop;
import mini_python.syntax.visitors.TVisitor;

/**
 * Operations with arity 2
 */
public class TEbinop extends TExpr {
    public final Binop op;
    public final TExpr e1, e2;

    public TEbinop(Binop op, TExpr e1, TExpr e2) throws InvalidBinopTypesException {
        super(op.coerce(e1.getType(), e2.getType()), true);
        this.op = op;
        this.e1 = e1;
        this.e2 = e2;

        if (this.getType() == null) {
            throw new InvalidBinopTypesException(null, op, this.e1.getType(), this.e2.getType());
        }
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}