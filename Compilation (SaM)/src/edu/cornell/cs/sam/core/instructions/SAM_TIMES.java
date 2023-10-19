package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Multiplies two integers
 */

public class SAM_TIMES extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() * mem.popINT());
		cpu.inc(PC);
	}
}
