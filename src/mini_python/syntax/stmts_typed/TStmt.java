package mini_python.syntax.stmts_typed;

import mini_python.syntax.visitors.TVisitor;

public abstract class TStmt {
    public abstract void accept(TVisitor v);
}
