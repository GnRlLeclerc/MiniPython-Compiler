package mini_python.syntax.exprs_typed;

import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

/**
 * Create a list from a range
 */
public class TErange extends TExpr {
    public final TExpr e;

    public TErange(TExpr e) {
        super(Type.RANGE, true);
        this.e = e;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
