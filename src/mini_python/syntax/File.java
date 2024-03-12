package mini_python.syntax;

import java.util.LinkedList;

import mini_python.syntax.stmts.Stmt;

/*
 * Typed trees.
 *
 * This is the output of the type checker and the input of the code
 * generation.
 *
 * In the typed trees, identifiers (objects of class `Ident`) are
 * now turned into objects of class `Variable` or `Function`.
 *
 * There is also a new class `TErange` for the Python expression
 * `list(range(e))`.
 */

public class File {
    public final LinkedList<Def> l;
    public final Stmt s; // a block of global statements

    public File(LinkedList<Def> l, Stmt s) {
        super();
        this.l = l;
        this.s = s;
    }
}
