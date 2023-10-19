package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import static edu.cornell.cs.sam.core.Memory.Data;

/**
 * Swaps the top two values of the stack
 */

public class SAM_SWAP extends SamInstruction {
	public void exec() throws SystemException {
		Data two = mem.pop();
		Data one = mem.pop();
		mem.push(two);
		mem.push(one);
		cpu.inc(PC);
	}
}

