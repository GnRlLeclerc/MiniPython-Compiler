package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.exprs.Expr;
import mini_python.syntax.visitors.Visitor;

public class Sreturn extends Stmt {
    public final Expr e;

    public Sreturn(Expr e) {
        super();
        this.e = e;
    }

    @Override
    public void accept(Visitor v) throws CompilationException {
        v.visit(this);
    }
}