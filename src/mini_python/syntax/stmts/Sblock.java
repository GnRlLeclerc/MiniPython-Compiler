package mini_python.syntax.stmts;

import java.util.LinkedList;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.visitors.Visitor;

/**
 * Block of statements (scope)
 */
public class Sblock extends Stmt {
    public final LinkedList<Stmt> l;

    public Sblock() {
        this.l = new LinkedList<Stmt>();
    }

    public Sblock(LinkedList<Stmt> l) {
        super();
        this.l = l;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}