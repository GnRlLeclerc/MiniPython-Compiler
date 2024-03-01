package mini_python.libc;

/**
 * Enum that holds the extended libc functions.
 * Every functions from the extended libc needs a 16-byte aligned stack before being called,
 * because they might call other functions that require this alignment.
 */
public enum ExtendedLibc {

	PRINTLN_INT64("println_int64"),
	PRINTLN_BOOL("println_bool"),
	PRINTLN_STRING("println_string"),
	PRINTLN_NONE("println_none"),
	ALLOC_OR_PANIC("alloc_or_panic"),
	PRINTLN_DYNAMIC("println_dynamic");

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
