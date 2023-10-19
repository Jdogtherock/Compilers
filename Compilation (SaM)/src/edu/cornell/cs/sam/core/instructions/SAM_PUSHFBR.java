package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the FBR on the stack
 */

public class SAM_PUSHFBR extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushMA(cpu.get(FBR));
		cpu.inc(PC);
	}
}
