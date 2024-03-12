package mini_python.syntax.stmts_typed;

import java.util.LinkedList;

import mini_python.syntax.visitors.TVisitor;

/**
 * Block of statements (scope)
 */
public class TSblock extends TStmt {
    public final LinkedList<TStmt> l;

    public TSblock() {
        this.l = new LinkedList<TStmt>();
    }

    TSblock(LinkedList<TStmt> l) {
        super();
        this.l = l;
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }
}