package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Stops execution
 */

public class SAM_STOP extends SamInstruction {
	public void exec() throws SystemException {
		cpu.set(HALT, 1);
	}
}

