package mini_python.syntax.visitors;

import mini_python.syntax.constants.Cbool;
import mini_python.syntax.constants.Cint;
import mini_python.syntax.constants.Cnone;
import mini_python.syntax.constants.Cstring;
import mini_python.syntax.exprs_typed.TEbinop;
import mini_python.syntax.exprs_typed.TEcall;
import mini_python.syntax.exprs_typed.TEcst;
import mini_python.syntax.exprs_typed.TEget;
import mini_python.syntax.exprs_typed.TEident;
import mini_python.syntax.exprs_typed.TElist;
import mini_python.syntax.exprs_typed.TErange;
import mini_python.syntax.exprs_typed.TEunop;
import mini_python.syntax.stmts_typed.TSassign;
import mini_python.syntax.stmts_typed.TSblock;
import mini_python.syntax.stmts_typed.TSeval;
import mini_python.syntax.stmts_typed.TSfor;
import mini_python.syntax.stmts_typed.TSif;
import mini_python.syntax.stmts_typed.TSprint;
import mini_python.syntax.stmts_typed.TSreturn;
import mini_python.syntax.stmts_typed.TSset;

public interface TVisitor {
    void visit(Cnone c);

    void visit(Cbool c);

    void visit(Cstring c);

    void visit(Cint c);

    void visit(TEcst e);

    void visit(TEbinop e);

    void visit(TEunop e);

    void visit(TEident e);

    void visit(TEcall e);

    void visit(TEget e);

    void visit(TElist e);

    void visit(TErange e);

    void visit(TSif s);

    void visit(TSreturn s);

    void visit(TSassign s);

    void visit(TSprint s);

    void visit(TSblock s);

    void visit(TSfor s);

    void visit(TSeval s);

    void visit(TSset s);
}
