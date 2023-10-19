package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the value at the address on top of the stack
 */

public class SAM_PUSHIND extends SamInstruction {
	public void exec() throws SystemException {
		mem.push(mem.getMem(mem.popMA()));
		cpu.inc(PC);
	}
}
