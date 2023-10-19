package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Subtracts two floating point numbers
 */

public class SAM_SUBF extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushFLOAT(-mem.popFLOAT() + mem.popFLOAT());
		cpu.inc(PC);
	}
}
