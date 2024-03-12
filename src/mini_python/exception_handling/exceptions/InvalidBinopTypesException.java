
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Location;
import mini_python.syntax.operations.Binop;
import mini_python.typing.Type;

public class InvalidBinopTypesException extends CompilationException {

    protected Binop op;
    protected Type type1;
    protected Type type2;

    public InvalidBinopTypesException(Location location, Binop op, Type type1, Type type2) {
        super(location);

        this.op = op;
        this.type1 = type1;
        this.type2 = type2;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid types %s and %s for operand %s", type1, type2, op);
    }
}
