package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Tuple;
import mini_python.syntax.Location;

public class RangeExpectedException extends CompilationException {

    protected String name;

    public RangeExpectedException(Location location) {
        super(location);

    }

    @Override
    public String getMessage() {
        return "`list` function expects a `range()` function call as argument";
    }

    @Override
    public String getErrorHelper() {
        return "`list` function called here";
    }

    @Override
    public Tuple<Integer, Integer> getIndicatorSpan() {

        return new Tuple<Integer, Integer>(location.column, location.column + "list".length());
    }
}
