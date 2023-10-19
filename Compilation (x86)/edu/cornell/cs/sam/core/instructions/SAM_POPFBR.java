package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pops the FBR off the stack
 */

public class SAM_POPFBR extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(FBR, mem.popMA());
		cpu.inc(PC);
	}
}
