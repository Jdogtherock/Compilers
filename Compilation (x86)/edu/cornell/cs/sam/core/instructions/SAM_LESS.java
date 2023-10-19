package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Checks if the bottom integer is smaller than the top integer
 */

public class SAM_LESS extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() > mem.popINT() ? 1 : 0);
		cpu.inc(PC);
	}
}


