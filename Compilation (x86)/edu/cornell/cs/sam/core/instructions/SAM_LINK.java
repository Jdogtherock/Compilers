package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Sets the FBR to the current SP, saving the old FBR
 * on the stack
 */

public class SAM_LINK extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushMA(cpu.get(FBR));
		cpu.set(FBR, cpu.get(SP) - 1);
		cpu.inc(PC);
	}
}
