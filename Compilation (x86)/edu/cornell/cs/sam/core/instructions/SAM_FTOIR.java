package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Converts a floating point number to an integer by rounding.
 */

public class SAM_FTOIR extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(Math.round(mem.popFLOAT()));
		cpu.inc(PC);
	}
}


