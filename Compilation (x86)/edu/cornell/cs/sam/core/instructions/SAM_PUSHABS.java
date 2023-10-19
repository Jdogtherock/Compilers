package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the value at the operand address on the stack 
 */

public class SAM_PUSHABS extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.push(mem.getMem(op));
		cpu.inc(PC);
	}
}
