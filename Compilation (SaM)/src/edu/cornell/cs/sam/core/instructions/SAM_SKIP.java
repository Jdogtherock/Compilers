package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pops a number off the stack, and skips that many instructions
 */

public class SAM_SKIP extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(PC, cpu.get(PC) + mem.popINT() + 1);
	}
}
