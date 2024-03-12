package mini_python.syntax.operations;

import mini_python.typing.Type;

public enum Binop {
    Badd("addition"), Bsub("subtraction"), Bmul("multiplication"), Bdiv("division"), Bmod("modulo"),
    Beq("equality"), Bneq("difference"), Blt("comparison"), Ble("comparison"), Bgt("comparison"), Bge("comparison"),
    Band("intersection"), Bor("union");

    public final String opName;

    Binop(String opName) {
        this.opName = opName;
    }

    /**
     * Given two operand types, return the output type
     * Returns null if the operation is invalid
     */
    public Type coerce(Type type1, Type type2) {

        // Basic operations or comparisons
        if (this == Band || this == Bor || this == Beq || this == Bneq) {
            return Type.BOOL; // All types can be coerced to bool, and these operations return a bool
        }

        // Ordering operations : the types must be coerccible together
        Type type = type1.coerce(type2);
        if (type != null && (this == Blt || this == Ble || this == Bgt || this == Bge)) {
            return Type.BOOL;
        }

        if (type != null && (this == Badd)) {
            return type;
        }

        // NOTE: we do not fully implement all python operations. The following can only
        // be done with int-type operands
        if ((type == Type.BOOL || type == Type.INT64 || type == Type.DYNAMIC)
                && (this == Bmul || this == Bdiv || this == Bmod || this == Bsub)) {
            return Type.INT64;
        }

        if (((type1 == Type.STRING && type2 == Type.INT64) || (type2 == Type.STRING && type1 == Type.INT64))
                && (this == Bmul)) {
            return Type.STRING;
        }

        if (((type1 == Type.LIST && type2 == Type.INT64) || (type2 == Type.LIST && type1 == Type.INT64))
                && (this == Bmul)) {
            return Type.LIST;
        }

        return null; // Default output: coercion failed
    }
}
