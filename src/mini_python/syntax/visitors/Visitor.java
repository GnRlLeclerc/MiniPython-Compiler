package mini_python.syntax.visitors;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.constants.Cbool;
import mini_python.syntax.constants.Cint;
import mini_python.syntax.constants.Cnone;
import mini_python.syntax.constants.Cstring;
import mini_python.syntax.exprs.Ebinop;
import mini_python.syntax.exprs.Ecall;
import mini_python.syntax.exprs.Ecst;
import mini_python.syntax.exprs.Eget;
import mini_python.syntax.exprs.Eident;
import mini_python.syntax.exprs.Elist;
import mini_python.syntax.exprs.Eunop;
import mini_python.syntax.stmts.Sassign;
import mini_python.syntax.stmts.Sblock;
import mini_python.syntax.stmts.Seval;
import mini_python.syntax.stmts.Sfor;
import mini_python.syntax.stmts.Sif;
import mini_python.syntax.stmts.Sprint;
import mini_python.syntax.stmts.Sreturn;
import mini_python.syntax.stmts.Sset;

/* unary and binary operators */

public interface Visitor {
    void visit(Cnone c);

    void visit(Cbool c);

    void visit(Cstring c);

    void visit(Cint c);

    void visit(Ecst e);

    void visit(Ebinop e) throws CompilationException;

    void visit(Eunop e) throws CompilationException;

    void visit(Eident e) throws CompilationException;

    void visit(Ecall e) throws CompilationException;

    void visit(Eget e) throws CompilationException;

    void visit(Elist e) throws CompilationException;

    void visit(Sif s) throws CompilationException;

    void visit(Sreturn s) throws CompilationException;

    void visit(Sassign s) throws CompilationException;

    void visit(Sprint s) throws CompilationException;

    void visit(Sblock s) throws CompilationException;

    void visit(Sfor s) throws CompilationException;

    void visit(Seval s) throws CompilationException;

    void visit(Sset s) throws CompilationException;
}
