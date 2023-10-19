package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs a bitwise NAND on two stack locations
 */

public class SAM_BITNAND extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(~(mem.popINT() & mem.popINT()));
		cpu.inc(PC);
	}
}

