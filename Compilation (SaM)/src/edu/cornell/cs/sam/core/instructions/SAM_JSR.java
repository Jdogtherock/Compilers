package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Jumps to the opcode address, saving the old PC
 */

public class SAM_JSR extends SamAddressInstruction {
	public void exec() throws SystemException {
		mem.pushPA(cpu.get(PC) + 1);
		cpu.set(PC, op);
	}
}
