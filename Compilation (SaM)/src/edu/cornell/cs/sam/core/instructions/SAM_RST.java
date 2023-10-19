package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Resets execution to the address on top of the stack
 */

public class SAM_RST extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(PC, mem.popPA());
	}
}
