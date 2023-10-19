package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Performs modulus on two integers 
 */

public class SAM_MOD extends SamInstruction {
	public void exec() throws SystemException {
		long v1 = mem.popINT();
		long v2 = mem.popINT();
		mem.pushINT((int) (v2 % v1));
		cpu.inc(PC);
	}
}
