package mini_python.syntax.stmts;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.visitors.Visitor;

public abstract class Stmt {
    abstract public void accept(Visitor v) throws CompilationException;
}