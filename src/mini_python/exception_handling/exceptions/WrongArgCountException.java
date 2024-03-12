
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Def;
import mini_python.syntax.Location;

public class WrongArgCountException extends CompilationException {

    protected Location definitionLocation;
    protected Def definition;
    protected int given;

    public WrongArgCountException(Location callLocation, Def definition, int given) {
        super(callLocation);

        this.definition = definition;
        this.given = given;
    }

    @Override
    public String getMessage() {
        return "Undefined "; // TODO: show the args, show the source code, etc...
    }
}
