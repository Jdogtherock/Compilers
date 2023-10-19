package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Save the stack pointer on the stack
 */

public class SAM_PUSHSP extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushMA(cpu.get(SP));
		cpu.inc(PC);
	}
}
