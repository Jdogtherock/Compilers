package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Multiplies two floating point numbers
 */

public class SAM_TIMESF extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushFLOAT(mem.popFLOAT() * mem.popFLOAT());
		cpu.inc(PC);
	}
}
