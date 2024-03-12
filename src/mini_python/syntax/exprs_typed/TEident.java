package mini_python.syntax.exprs_typed;

import mini_python.syntax.Variable;
import mini_python.syntax.visitors.TVisitor;

/**
 * Access a variable value
 */
public class TEident extends TExpr {
    public final Variable x;

    public TEident(Variable x) {
        super(x.type, false);
        this.x = x;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}