package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Converts an integer to a floating point number
 */

public class SAM_ITOF extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushFLOAT(new Integer(mem.popINT()).floatValue());
		cpu.inc(PC);
	}
}

