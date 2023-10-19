package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Checks if the bottom integer is greater than the top integer
 */

public class SAM_GREATER extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() < mem.popINT() ? 1 : 0);
		cpu.inc(PC);
	}
}


