package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Stores the value on top of the stack at the operand address
 */

public class SAM_STOREABS extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.setMem(op, mem.pop());
		cpu.inc(PC);
	}
}

