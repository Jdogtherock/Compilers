package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Pushes the operand integer on the stack
 */

public class SAM_PUSHIMM extends SamIntInstruction {
	public void exec() throws SystemException {
		mem.pushINT(op);
		cpu.inc(PC);
	}
}
