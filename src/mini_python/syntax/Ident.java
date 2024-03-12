package mini_python.syntax;

/**
 * Function or variable identifier
 */
public class Ident {
    public final String id;
    public final Location loc;

    public Ident(String id) {
        this.id = id;
        this.loc = null;
    }

    public Ident(String id, Location loc) {
        this.id = id;
        this.loc = loc;
    }
}
