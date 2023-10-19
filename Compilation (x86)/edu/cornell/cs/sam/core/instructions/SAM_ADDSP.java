package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Adds an integer to the stack pointer
 */

public class SAM_ADDSP extends SamIntInstruction {
	public void exec() throws SystemException {
		cpu.set(SP, cpu.get(SP) + op);
		cpu.inc(PC);
	}
}


