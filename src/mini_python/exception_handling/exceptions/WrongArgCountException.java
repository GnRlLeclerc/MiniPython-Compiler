
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.terminal.Color;
import mini_python.syntax.Def;
import mini_python.syntax.Span;
import mini_python.syntax.exprs.Ecall;
import mini_python.syntax.exprs.Expr;

public class WrongArgCountException extends CompilationException {

    public final Def definition;
    public final Ecall call;

    public WrongArgCountException(Def definition, Ecall call) {
        super(call.f.loc);

        this.definition = definition;
        this.call = call;
    }

    @Override
    public String getMessage() {
        int expected = definition.l.size();
        int given = call.l.size();

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
        int expected = definition.l.size();
        int given = call.l.size();
        int delta = Math.abs(given - expected);
        int fend = call.f.loc.column + call.f.id.length();

        if (given < expected) {
            // Missing arguments
            int start = fend;
            int end = start + 2;
            if (given != 0) {
                start = call.l.getFirst().getSpan().start.column - 1;
                Expr last = call.l.getLast();
                end = last.getSpan().start.column + last.getSpan().length + 1;
            }

            return new StringBuilder()
                    .append(Color.BOLD_BLUE)
                    .append(" ".repeat(start - fend))
                    .append("-".repeat(end - start))
                    .append(" ")
                    .append(delta)
                    .append(delta == 1 ? " argument is missing" : " arguments are missing")
                    .toString();

        } else {
            // Extra arguments
            int start;
            if (expected == 0)
                start = call.l.get(expected).getSpan().start.column;
            else {
                Span previous = call.l.get(expected - 1).getSpan();
                start = previous.start.column + previous.length;
            }

            Expr last = call.l.getLast();
            int end = last.getSpan().start.column + last.getSpan().length;

            return new StringBuilder()
                    .append(Color.BOLD_BLUE)
                    .append(" ".repeat(start - fend))
                    .append("^".repeat(end - start))
                    .append(" ")
                    .append(delta)
                    .append(delta == 1 ? " extra argument" : " extra arguments")
                    .toString();
        }
    }

    @Override
    public Span getIndicatorSpan() {
        return new Span(location, definition.f.id.length());
    }
}
