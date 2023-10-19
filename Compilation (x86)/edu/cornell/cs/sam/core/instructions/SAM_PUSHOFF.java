package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pushes the value at the operand offset from the FBR on top of the stack
 */

public class SAM_PUSHOFF extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.push(mem.getMem(cpu.get(FBR) + op));
		cpu.inc(PC);
	}
}
