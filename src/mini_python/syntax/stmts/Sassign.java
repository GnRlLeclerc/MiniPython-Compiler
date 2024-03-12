package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Ident;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

/**
 * Assign a value to a variable
 */
public class Sassign extends Stmt {
    public final Ident x;
    public final Expr e;

    public Sassign(Ident x, Expr e) {
        super();
        this.x = x;
        this.e = e;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}