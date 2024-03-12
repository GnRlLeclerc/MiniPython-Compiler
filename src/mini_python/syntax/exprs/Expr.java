package mini_python.syntax.exprs;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.visitors.Visitor;

public abstract class Expr {
    abstract public void accept(Visitor v) throws CompilationException;
}