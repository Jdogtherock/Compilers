package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs bitwise XOR on two locations of the stack
 */

public class SAM_BITXOR extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() ^ mem.popINT());
		cpu.inc(PC);
	}
}
