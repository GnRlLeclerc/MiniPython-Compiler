package mini_python.syntax.constants;

import mini_python.syntax.Location;
import mini_python.syntax.visitors.TVisitor;
import mini_python.syntax.visitors.Visitor;
import mini_python.typing.Type;

/**
 * Constant (string, bool, int...)
 */
public abstract class Constant {
    public final Location location;

    public Constant(Location location) {
        this.location = location;
    }

    abstract public void accept(Visitor v);

    abstract public void accept(TVisitor v);

    public abstract Type getType();
}