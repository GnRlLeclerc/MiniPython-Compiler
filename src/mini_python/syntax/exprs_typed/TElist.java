package mini_python.syntax.exprs_typed;

import java.util.LinkedList;

import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

/**
 * Create a list from a list of expressions
 */
public class TElist extends TExpr {
    public final LinkedList<TExpr> l;

    public TElist(LinkedList<TExpr> l) {
        super(Type.LIST, true);
        this.l = l;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}