package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Converts a floating point value to an integer by truncating
 */

public class SAM_FTOI extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(new Float(mem.popFLOAT()).intValue());
		cpu.inc(PC);
	}
}


