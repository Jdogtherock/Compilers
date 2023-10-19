package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Shifts left the integer below the top of the stack
 * as many bits as specified by the top of the stack
 */

public class SAM_LSHIFTIND extends SamInstruction {
	public void exec() throws SystemException {
		int bits = mem.popINT();
		mem.pushINT(mem.popINT() << bits);
		cpu.inc(PC);
	}
}
