package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Checks if an integer is positive
 */

public class SAM_ISPOS extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() > 0 ? 1 : 0);
		cpu.inc(PC);
	}
}


