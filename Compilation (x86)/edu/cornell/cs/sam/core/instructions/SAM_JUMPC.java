package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Jumps to the opcode address if the top of the
 * stack is true (nonzero)
 */

public class SAM_JUMPC extends SamAddressInstruction {
	public void exec() throws SystemException {
		if (mem.popINT() != 0)
			cpu.set(PC, op);
		else
			cpu.inc(PC);
	}
}


