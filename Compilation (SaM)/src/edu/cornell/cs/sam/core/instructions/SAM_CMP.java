package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Compares two integers, and places -1, 0, or 1 on the stack 
 * if the first is respectively smaller, equal, or larger 
 * than the second (top)
 */

public class SAM_CMP extends SamInstruction {
	public void exec() throws SystemException {
		int high = mem.popINT();
		int low = mem.popINT();
		mem.pushINT(high > low ? 1 : high < low ? -1 : 0);
		cpu.inc(PC);
	}
}
