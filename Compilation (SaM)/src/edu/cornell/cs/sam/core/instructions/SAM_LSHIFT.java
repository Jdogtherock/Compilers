package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Shifts an integer op bits to the left
 */

public class SAM_LSHIFT extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() << op);
		cpu.inc(PC);
	}
}
