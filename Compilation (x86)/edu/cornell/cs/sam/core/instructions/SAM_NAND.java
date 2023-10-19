package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Performs logical NAND on two integers
 */

public class SAM_NAND extends SamInstruction {
	public void exec() throws SystemException {
		mem.pushINT(mem.popINT() == 0 | mem.popINT() == 0 ? 1 : 0);
		cpu.inc(PC);
	}
}
