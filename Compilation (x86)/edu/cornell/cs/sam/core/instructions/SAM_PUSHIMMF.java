package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the operand floating point number on the stack
 */

public class SAM_PUSHIMMF extends SamFloatInstruction {
	public void exec() throws SystemException {
		mem.pushFLOAT(op);
		cpu.inc(PC);
	}
}
