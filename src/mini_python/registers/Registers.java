package mini_python.registers;

/**
 * Register wrapper enum when typing is needed
 */
public enum Registers {

	RAX(Regs.RAX), RBX(Regs.RBX), RCX(Regs.RCX), RDX(Regs.RDX), RSI(Regs.RSI), RDI(Regs.RDI), R8(Regs.R8), R9(Regs.R9), R10(Regs.R10), R11(Regs.R11), R12(Regs.R12), R13(Regs.R13), R14(Regs.R14), R15(Regs.R15), RBP(Regs.RBP), RSP(Regs.RSP), RIP(Regs.RIP);

	private final String code;

	Registers(String code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return code;
	}
}
