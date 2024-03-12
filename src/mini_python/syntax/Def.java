package mini_python.syntax;

import java.util.LinkedList;

import mini_python.syntax.stmts.Stmt;

/*
 * visitor for the parsed trees
 * (feel free to modify it for your needs)
 */

/**
 * Function definition
 */
public class Def {
    public final Ident f;
    public final LinkedList<Ident> l; // formal parameters
    public final Stmt s;

    public Def(Ident f, LinkedList<Ident> l, Stmt s) {
        super();
        this.f = f;
        this.l = l;
        this.s = s;
    }
}