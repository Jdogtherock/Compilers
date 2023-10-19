package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Jumps to the address on top of the stack,
 * saving the old PC.
 */

public class SAM_JSRIND extends SamInstruction {
	public void exec() throws SystemException {
		int top = mem.popPA();
		mem.pushPA(cpu.get(PC) + 1);
		cpu.set(PC, top);
	}
}


