
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.exception_handling.Tuple;
import mini_python.syntax.Span;
import mini_python.syntax.operations.Unop;
import mini_python.typing.Type;

public class InvalidUnopTypeException extends CompilationException {

    protected Unop op;
    protected Type type;
    protected Span span;

    public InvalidUnopTypeException(Span span, Unop op, Type type) {
        super(span.start);

        this.op = op;
        this.type = type;
        this.span = span;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid type %s for operand %s", type, op);
    }

    @Override
    public String getErrorHelper() {
        return "TODO";
    }

    @Override
    public Tuple<Integer, Integer> getIndicatorSpan() {
        return new Tuple<Integer, Integer>(location.column, location.column + op.toString().length());
    }
}
