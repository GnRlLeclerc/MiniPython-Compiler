package mini_python.syntax.exprs_typed;

import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

/**
 * Get a value from a list.
 * e1 is the list expression or variable, and e2 is the index.
 */
public class TEget extends TExpr {
    public final TExpr e1, e2;

    public TEget(TExpr e1, TExpr e2) {
        super(Type.DYNAMIC, false); // List can contain multiple elements
        this.e1 = e1;
        this.e2 = e2;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}