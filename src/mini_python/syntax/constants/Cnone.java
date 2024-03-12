package mini_python.syntax.constants;

import mini_python.syntax.visitors.TVisitor;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

public class Cnone extends Constant {
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
        return Type.NONETYPE;
    }

}
