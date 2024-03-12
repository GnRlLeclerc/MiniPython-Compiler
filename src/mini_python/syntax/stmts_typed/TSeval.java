package mini_python.syntax.stmts_typed;

import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * Statement that evaluates to a value
 */
public class TSeval extends TStmt {
    public final TExpr e;

    public TSeval(TExpr e) {
        super();
        this.e = e;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
