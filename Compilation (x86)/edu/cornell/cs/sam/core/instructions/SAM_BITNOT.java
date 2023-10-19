package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs bitwise negation on the top of the stack
 */

public class SAM_BITNOT extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(~mem.popINT());
		cpu.inc(PC);
	}
}
