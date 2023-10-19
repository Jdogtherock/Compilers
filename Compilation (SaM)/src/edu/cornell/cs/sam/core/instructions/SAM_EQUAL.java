package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Checks if two values are equal
 */

public class SAM_EQUAL extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popValue() == mem.popValue() ? 1 : 0);
		cpu.inc(PC);
	}
}
