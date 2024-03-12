
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Location;
import mini_python.syntax.operations.Unop;
import mini_python.typing.Type;

public class InvalidUnopTypeException extends CompilationException {

    protected Unop op;
    protected Type type;

    public InvalidUnopTypeException(Location location, Unop op, Type type) {
        super(location);

        this.op = op;
        this.type = type;
        // TODO: list the expected types for the operation (for unops this is simple
        // enough)
    }

    @Override
    public String getMessage() {
        return "Invalid type " + type + " for operand " + op + " at " + location;
    }
}
