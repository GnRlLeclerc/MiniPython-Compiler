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
	DIV_DYNAMIC("div_dynamic"),
	MOD_DYNAMIC("mod_dynamic"),
	AND_DYNAMIC("and_dynamic"),
	OR_DYNAMIC("or_dynamic"),
	MUL_DYNAMIC("mul_dynamic"),
	NOT_DYNAMIC("not_dynamic"),
	NEG_DYNAMIC("neg_dynamic"),
	EQ_DYNAMIC("eq_dynamic"),
	NEQ_DYNAMIC("neq_dynamic"),
	LT_DYNAMIC("lt_dynamic"),
	LE_DYNAMIC("le_dynamic"),
	GT_DYNAMIC("gt_dynamic"),
	GE_DYNAMIC("ge_dynamic"),
	GET_ELEMENT("get_element"),
	LEN_DYNAMIC("len_dynamic"),
	TRUTHY_DYNAMIC("truthy_dynamic"),
	SET_ELEMENT("set_element"),
	RANGE_LIST("range_list");
	
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
