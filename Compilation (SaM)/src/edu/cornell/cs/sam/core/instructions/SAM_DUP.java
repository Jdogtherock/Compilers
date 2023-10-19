package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import static edu.cornell.cs.sam.core.Memory.Data;

/**
 * Duplicates the top stack value
 */

public class SAM_DUP extends SamInstruction {
	public void exec() throws SystemException {	
		Data top = mem.pop();
		mem.push(top);
		mem.push(top);
		cpu.inc(PC);
	}
}


