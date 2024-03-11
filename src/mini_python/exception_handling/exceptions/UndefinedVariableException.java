package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Location;

public class UndefinedVariableException extends CompilationException {

    protected String name;

    public UndefinedVariableException(String name, Location location) {
        super(location);
        this.name = name;
    }

    @Override
    public String getMessage() {
        return "Undefined variable " + name + " at " + location;
    }
}
