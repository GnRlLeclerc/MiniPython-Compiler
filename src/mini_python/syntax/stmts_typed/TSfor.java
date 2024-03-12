package mini_python.syntax.stmts_typed;

import mini_python.syntax.Variable;
import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * For loop
 * for (x in e) { s }
 */
public class TSfor extends TStmt {
    public final Variable x;
    public final TExpr e;
    public final TStmt s;

    public TSfor(Variable x, TExpr e, TStmt s) {
        super();
        this.x = x;
        this.e = e;
        this.s = s;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}