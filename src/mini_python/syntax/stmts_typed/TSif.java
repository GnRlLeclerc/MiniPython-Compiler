package mini_python.syntax.stmts_typed;

import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * If block.
 * If (e) { s1 } else { s2 }
 */
public class TSif extends TStmt {
    public final TExpr e;
    public final TStmt s1, s2;

    public TSif(TExpr e, TStmt s1, TStmt s2) {
        super();
        this.e = e;
        this.s1 = s1;
        this.s2 = s2;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}