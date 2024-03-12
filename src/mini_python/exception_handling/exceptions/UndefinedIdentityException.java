package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Tuple;
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
        return String.format("cannot find %s `%s` in this scope", name, identity);
    }

    @Override
    public String getErrorHelper() {
        return String.format("`%s` is undefined", name);
    }

    @Override
    public Tuple<Integer, Integer> getIndicatorSpan() {
        return new Tuple<Integer, Integer>(location.column, location.column + name.length());
    }
}
