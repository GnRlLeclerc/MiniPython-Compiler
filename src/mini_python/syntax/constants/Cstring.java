package mini_python.syntax.constants;

import mini_python.syntax.Location;
import mini_python.syntax.visitors.TVisitor;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

/* expressions */

public class Cstring extends Constant {
    public final String s;

    public Cstring(Location loc, String s) {
        super(loc);
        this.s = s;
    }

    @Override
    public void accept(Visitor v) {
        v.visit(this);
    }

    @Override
    public void accept(TVisitor v) {
        v.visit(this);
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public int length() {
        return this.s.length() + 3; // Add 2 for the quotes + 1 because the end is not included
    }
}
