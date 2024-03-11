package mini_python.exception_handling;

/** The base compilation exception class */
public abstract class CompilationException extends Exception {
    protected Location location;

    public CompilationException(Location location) {
        this.location = location;
    }

    public abstract String getMessage();
}
