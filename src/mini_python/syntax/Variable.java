package mini_python.syntax;

import mini_python.typing.Type;

/*
 * In the typed trees, all the occurrences of the same variable
 * point to a single object of the following class.
 */
public class Variable {
    private static int id = 0;
    public final String name; // for debugging purposes
    public Type type; // The type of a variable can be dynamically set, and can change on reassign.
    public int uid; // unique id, for debugging purposes
    public int ofs; // position wrt %rbp

    private Variable(String name, int uid, Type type, int offset) {
        this.name = name;
        this.uid = uid;
        this.ofs = offset; // Offset of 0 with regards to %rbp

        // Type can be `null` ! If the type is `null` when trying to access the
        // variable,
        // this means that the variable is being accessed before being assigned !
        this.type = type;
    }

    /**
     * Create a new variable with default type NoneType
     */
    public static Variable mkVariable(String name, int offset) {
        return new Variable(name, id++, Type.DYNAMIC, offset);
    }

    /**
     * Create a new variable with a given type
     */
    public static Variable mkVariable(String name, Type type, int offset) {
        return new Variable(name, id++, type, offset);
    }

    @Override
    public String toString() {
        return this.name + " : " + this.type;
    }
}
