package mini_python.syntax.stmts_typed;

import mini_python.syntax.Variable;
import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;

/**
 * Assign a value to a variable
 */
public class TSassign extends TStmt {
    public final Variable x;
    public final TExpr e;

    public TSassign(Variable x, TExpr e) {
        super();
        this.x = x;
        this.e = e;
        this.e.temporary = false; // The value is not temporary anymore
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
