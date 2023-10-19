package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Pushes the operand character on the stack
 */

public class SAM_PUSHIMMCH extends SamCharInstruction {
	public void exec() throws SystemException {
		mem.pushCH(op);
		cpu.inc(PC);
	}
}
