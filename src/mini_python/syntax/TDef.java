package mini_python.syntax;

import mini_python.syntax.stmts_typed.TStmt;

/**
 * Function definition
 */
public class TDef {
    public final Function f;
    public final TStmt body;

    public TDef(Function f, TStmt body) {
        super();
        this.f = f;
        this.body = body;
    }
}
