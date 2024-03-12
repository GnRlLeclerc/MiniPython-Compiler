
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
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
        return String.format("Invalid type %s for %s operator", type, op.opName);
    }

    @Override
    public String getErrorHelper() {
        return String.format("%s with type %s here", op.opName, type);
    }

    @Override
    public Span getIndicatorSpan() {
        return span;
    }
}
