package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Stores the value on top of the stack
 * at the operand offset from the FBR
 */

public class SAM_STOREOFF extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.setMem(cpu.get(FBR) + op, mem.pop());
		cpu.inc(PC);
	}
}
