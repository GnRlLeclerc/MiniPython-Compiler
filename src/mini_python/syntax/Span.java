package mini_python.syntax;

/** Expression span over the source file, for precise error messages */
public class Span {
    public final Location start;
    public final int length;

    public Span(Location start, int length) {
        this.start = start;
        this.length = length;
    }
}
