package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Restores the FBR from the stack
 */

public class SAM_UNLINK extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(FBR, mem.popMA());
		cpu.inc(PC);
	}
}
