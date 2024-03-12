package mini_python.syntax.stmts_typed;

import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * Print a value to standard output
 */
public class TSprint extends TStmt {
    public final TExpr e;

    public TSprint(TExpr e) {
        super();
        this.e = e;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
