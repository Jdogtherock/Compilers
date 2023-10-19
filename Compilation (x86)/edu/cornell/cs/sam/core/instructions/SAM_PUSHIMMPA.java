package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the operand program address on the stack
 */

public class SAM_PUSHIMMPA extends SamAddressInstruction {
	public void exec() throws SystemException {
		mem.pushPA(op);
		cpu.inc(PC);
	}
}
