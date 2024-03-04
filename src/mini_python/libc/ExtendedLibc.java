package mini_python.libc;

/**
 * Enum that holds the extended libc functions.
 * Every functions from the extended libc needs a 16-byte aligned stack before being called,
 * because they might call other functions that require this alignment.
 */
public enum ExtendedLibc {

	PRINTLN_DYNAMIC("println_dynamic"),
	ADD_DYNAMIC("add_dynamic"),
	SUB_DYNAMIC("sub_dynamic"),
	LT_DYNAMIC("lt_dynamic");
	private final String label;

	ExtendedLibc(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		return label;
	}

	public String getLabel() {
		return label;
	}
}
