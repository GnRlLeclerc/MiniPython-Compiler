package mini_python.exception_handling;

import mini_python.syntax.Location;

/** The base compilation exception class */
public abstract class CompilationException extends Exception {
    protected Location location;

    public CompilationException(Location location) {
        this.location = location;
    }

    public abstract String getMessage();
}
