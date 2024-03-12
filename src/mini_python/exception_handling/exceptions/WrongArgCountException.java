
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Def;
import mini_python.syntax.Location;
import mini_python.syntax.Span;

public class WrongArgCountException extends CompilationException {

    public final Def definition;
    public final int given;

    public WrongArgCountException(Location callLocation, Def definition, int given) {
        super(callLocation);

        this.definition = definition;
        this.given = given;

        // Problème : on n'a pas les expr d'input ? On pourrait calculer un span de
        // chacun
        // NOTE: pour les trucs

        // 1. Pour le "function defined here" avec underlining des paramètres, ça on
        // peut faire facilement
        // déjà, faire ça. On verra le reste après.
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
    public Span getIndicatorSpan() {
        return new Span(location, definition.f.id.length());
    }
}
