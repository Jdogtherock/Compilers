package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Checks if a value is zero
 */

public class SAM_ISNIL extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() == 0 ? 1 : 0);
		cpu.inc(PC);
	}
}


