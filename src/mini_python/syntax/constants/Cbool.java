package mini_python.syntax.constants;

import mini_python.syntax.visitors.TVisitor;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

public class Cbool extends Constant {
    public final boolean b;

    public Cbool(boolean b) {
        this.b = b;
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
        return Type.BOOL;
    }
}
