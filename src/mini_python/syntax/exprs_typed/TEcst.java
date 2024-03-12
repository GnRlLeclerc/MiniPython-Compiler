package mini_python.syntax.exprs_typed;

import mini_python.syntax.constants.Constant;
import mini_python.syntax.visitors.TVisitor;

/**
 * Constant expressions
 */
public class TEcst extends TExpr {
    public final Constant c;

    public TEcst(Constant c) {
        super(c.getType(), true);
        this.c = c;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}