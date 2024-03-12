package mini_python.syntax.exprs_typed;

import mini_python.syntax.visitors.TVisitor;
import mini_python.typing.Type;

/**
 * Abstract base class for typed expressions.
 * As we use static typing, every expression has a type.
 */
public abstract class TExpr {

    public Type type;
    // denotes if the value in the expression is temporary or
    // in scope as a variable or list element (and not an
    // intermediate value for example)
    // we assume every expression is temporary by default
    // unless : the expression is a variable or list access
    // or the expression is linked to a variable or list assignement
    // where the temporary is updated on creation of the assigment
    public boolean temporary;

    TExpr(Type type, boolean temporary) {
        assert type != null;
        this.type = type;
        this.temporary = temporary;
    }

    abstract public void accept(TVisitor v);

    public Type getType() {
        return type;
    }
}