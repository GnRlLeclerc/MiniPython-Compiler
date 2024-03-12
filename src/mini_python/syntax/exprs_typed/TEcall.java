package mini_python.syntax.exprs_typed;

import java.util.LinkedList;

import mini_python.syntax.Function;
import mini_python.syntax.visitors.TVisitor;

/**
 * Call a function
 */
public class TEcall extends TExpr {
    public final Function f;
    public final LinkedList<TExpr> l;

    public TEcall(Function f, LinkedList<TExpr> l) {
        super(f.returnType, false);
        this.f = f;
        this.l = l;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
