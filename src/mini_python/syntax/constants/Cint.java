package mini_python.syntax.constants;

import mini_python.syntax.visitors.TVisitor;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

public class Cint extends Constant {
    public final long i; // Python has arbitrary-precision integers; we simplify here

    public Cint(long i) {
        this.i = i;
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
        return Type.INT64;
    }
}