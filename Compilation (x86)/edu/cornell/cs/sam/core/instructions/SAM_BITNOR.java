package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs bitwise NOR on two locations on the stack
 */

public class SAM_BITNOR extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(~(mem.popINT() | mem.popINT()));
		cpu.inc(PC);
	}
}
