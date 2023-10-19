package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Jumps to the opcode address
 */

public class SAM_JUMP extends SamAddressInstruction {
	public void exec() throws SystemException {
		cpu.set(PC, op);
	}
}


