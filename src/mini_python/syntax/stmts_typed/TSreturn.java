package mini_python.syntax.stmts_typed;

import mini_python.syntax.exprs_typed.TExpr;
import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

public class TSreturn extends TStmt {
    public final TExpr e;

    // The default return type is the same as the expression.
    // However, when a function can return multiple types, we use the "dynamic"
    // type.
    // In this case, the TSreturn statement will have "dynamic" set as its return
    // type, and will know to convert
    // the original statement type to the dynamic one.
    public Type returnType;

    public TSreturn(TExpr e) {
        super();
        this.returnType = e.getType();
        this.e = e;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}
