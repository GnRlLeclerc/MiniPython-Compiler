package mini_python.exception_handling;

import mini_python.syntax.Location;

/** The base compilation exception class */
public abstract class CompilationException extends Exception {
    protected Location location;

    public CompilationException(Location location) {
        this.location = location;
    }

    public abstract String getMessage();

    /** Error message displayed right under the erroneous code */
    public abstract String getErrorHelper();

    /** Start and end columns for the erroneous code line */
    public abstract Tuple<Integer, Integer> getIndicatorSpan();
}
