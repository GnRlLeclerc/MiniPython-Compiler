
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Tuple;
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
        int expected = definition.l.size();

        if (given == 1) {
            return String.format("this function takes %s argument%s but 1 argument was supplied", expected,
                    expected == 1 ? "" : 's');
        } else {
            return String.format("this function takes %s argument%s but %s arguments were supplied", expected,
                    expected == 1 ? "" : 's', given);
        }
    }

    @Override
    public String getErrorHelper() {
        return String.format("`%s` function called here", definition.f.id);
    }

    @Override
    public Tuple<Integer, Integer> getIndicatorSpan() {
        return new Tuple<Integer, Integer>(location.column, location.column + definition.f.id.length());
    }
}
