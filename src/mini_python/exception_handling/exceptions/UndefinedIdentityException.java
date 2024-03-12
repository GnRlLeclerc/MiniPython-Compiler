package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Location;

public class UndefinedIdentityException extends CompilationException {

    protected String name;
    protected String identity;

    public UndefinedIdentityException(String name, String identity, Location location) {
        super(location);
        this.name = name;
        this.identity = identity;
    }

    @Override
    public String getMessage() {
        return "Undefined " + identity + " " + name + " at " + location;
    }
}
