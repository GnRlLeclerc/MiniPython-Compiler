package mini_python.typing;

/**
 * Valid static types for the mini-python language
 */
public enum Type {
	INT64("int64"),
	BOOL("bool"),
	STRING("str"),
	NONETYPE("NoneType"),
	RANGE("range"),
	LIST("list"),
	DYNAMIC("dynamic");

	private final String name;

	Type(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}

	/**
	 * Given 2 types, find the resulting type of their operation.
	 * Returns `null` if the types cannot be coerced together.
	 */
	public Type coerce(Type other) {
		// If the types are the same, return the type
		if (this == other) {
			return this;
		}

		// If one is dynamic, we cannot know (example: bool + dynamic might be int64 if dynamic is int64)
		if (this == DYNAMIC || other == DYNAMIC) {
			return DYNAMIC;
		}

		// Bool & int64 can be coerced together
		if ((this == INT64 && other == BOOL) || (this == BOOL && other == INT64)) {
			return INT64;
		}
		
		return null; // Default output: coercion failed
	}
}
