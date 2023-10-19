package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Adds two floating point numbers on the stack
 */

public class SAM_ADDF extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushFLOAT(mem.popFLOAT() + mem.popFLOAT());
		cpu.inc(PC);
	}
}
