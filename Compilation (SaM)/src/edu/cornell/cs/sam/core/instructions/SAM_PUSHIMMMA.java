package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the operand memory address on the stack
 */

public class SAM_PUSHIMMMA extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.pushMA(op);
		cpu.inc(PC);
	}
}
