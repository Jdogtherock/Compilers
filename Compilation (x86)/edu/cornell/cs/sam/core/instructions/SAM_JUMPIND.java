package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Jumps to the address on top of the stack
 */

public class SAM_JUMPIND extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(PC, mem.popPA());
	}
}
