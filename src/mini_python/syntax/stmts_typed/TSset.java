package mini_python.syntax.stmts_typed;

import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * Assign a value to a list element
 */

public class TSset extends TStmt {
    public final TExpr e1, e2, e3;

    public TSset(TExpr e1, TExpr e2, TExpr e3) {
        super();
        this.e1 = e1;
        /* function definition and file */
        this.e2 = e2;
        this.e3 = e3;
        this.e3.temporary = false; // The value is not temporary anymore
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
