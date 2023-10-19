package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Compares two floating pointer numbers, and places
 * -1, 0, or 1 on the stack if the first is
 * respectively smaller, equal, or larger than the second (top)
 */

public class SAM_CMPF extends SamInstruction {
	public void exec() throws SystemException {
		float high = mem.popFLOAT();
		float low = mem.popFLOAT();
		mem.pushINT(high > low ? 1 : high < low ? -1 : 0);
		cpu.inc(PC);
	}
}


