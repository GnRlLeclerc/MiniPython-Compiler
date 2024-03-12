package mini_python.exception_handling;

import java.io.Serial;

// the following exception is used whenever you have to implement something
public class Todo extends Error {
    @Serial
    private static final long serialVersionUID = 1L;

    public Todo(String message) {
        super("TODO: " + message);
    }

    Todo() {
        super("TODO");
    }
}