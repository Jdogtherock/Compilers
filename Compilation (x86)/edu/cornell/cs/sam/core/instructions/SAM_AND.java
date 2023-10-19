package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs a logical AND on two stack locations
 */

public class SAM_AND extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT((mem.popINT() == 0 | mem.popINT() == 0)? 0: 1);
		cpu.inc(PC);
	}
}
