package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs logical NOR on two integers
 */

public class SAM_NOR extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() != 0 | mem.popINT() != 0 ? 0 : 1);
		cpu.inc(PC);
	}
}
