
package mini_python.exception_handling.exceptions;

import mini_python.exception_handling.CompilationException;
import mini_python.syntax.Span;
import mini_python.syntax.operations.Binop;
import mini_python.typing.Type;

public class InvalidBinopTypesException extends CompilationException {

    protected Binop op;
    protected Type type1;
    protected Type type2;
    protected Span span;

    public InvalidBinopTypesException(Span span, Binop op, Type type1, Type type2) {
        super(span.start);

        this.op = op;
        this.type1 = type1;
        this.type2 = type2;
        this.span = span;
    }

    @Override
    public String getMessage() {
        return String.format("Invalid types %s and %s for %s", type1, type2, op.opName);
    }

    @Override
    public String getErrorHelper() {
        return String.format(" %s between types %s and %s here", op.opName, type1, type2);
    }

    @Override
    public Span getIndicatorSpan() {
        return span;
    }
}
