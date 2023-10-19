package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Shifts an integer op bits to the right
 */

public class SAM_RSHIFT extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() >> op);
		cpu.inc(PC);
	}
}
