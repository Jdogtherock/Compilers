package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs logical XOR on two integers
 */

public class SAM_XOR extends SamInstruction {
	public void exec() throws SystemException {
		boolean one = (mem.popINT() == 0) ? false : true;
		boolean two = (mem.popINT() == 0) ? false : true;
		mem.pushINT((one && !two) || (two && !one) ? 1 : 0);
		cpu.inc(PC);
	}
}
