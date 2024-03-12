package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Location;

public class RangeExpectedException extends CompilationException {

    protected String name;
    protected String identity;

    public RangeExpectedException(Location location) {
        super(location);

    }

    @Override
    public String getMessage() {
        return "`list` function expects a `range()` function call as argument";
    }
}
