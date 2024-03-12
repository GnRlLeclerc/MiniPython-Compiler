package mini_python.syntax;

/**
 * Code location in the source file
 */
public class Location {
	public final int line;
	public final int column;

	public Location(int line, int column) {
		this.line = line + 1;
		this.column = column;
	}

	@Override
	public String toString() {
		return this.line + ":" + this.column + ":";
	}
}