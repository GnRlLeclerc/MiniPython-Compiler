package mini_python.syntax;

import java.util.LinkedList;

import mini_python.typing.Type;

/*
 * Similarly, all the occurrences of a given function all point
 * to a single object of the following class.
 */
public class Function {
    public final String name;
    public final LinkedList<Variable> params;

    // The return type of a function will be determined after having parsed all of
    // its return statements.
    // If all types can be cast to one, a static type will be used. Else, the return
    // type will be dynamic.
    public Type returnType;

    // Stores the byte offset from the base stack frame in order to allocate enough
    // stack space
    // for all function arguments + all local variables for this function.
    // This is determined during the type checking phase, where every local variable
    // receives a stack frame offset.
    public int localVariablesOffset; // Negative bytes

    public Function(String name, LinkedList<Variable> params) {
        this.name = name;
        this.params = params;
        this.returnType = Type.DYNAMIC; // By default (we cannot assume what it will be)
        this.localVariablesOffset = 0; // Offset for local variables.
        // We do not initialize it to - len * 8 here because we need to assign the
        // offset to the `Variable` objects too
    }

    /**
     * Get the stack frame offset position for a new variable local to a function
     */
    public int getStackFrameOffset() {
        this.localVariablesOffset -= 8;
        return this.localVariablesOffset;
    }
}