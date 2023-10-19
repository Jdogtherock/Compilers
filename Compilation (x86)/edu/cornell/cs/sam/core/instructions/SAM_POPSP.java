package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Restores the SP to the top value on the stack
 */

public class SAM_POPSP extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(SP, mem.popMA());
		cpu.inc(PC);
	}
}
