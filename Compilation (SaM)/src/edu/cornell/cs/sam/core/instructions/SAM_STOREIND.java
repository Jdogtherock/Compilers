package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import static edu.cornell.cs.sam.core.Memory.Data;

/**
 * Stores the value on top of the stack
 * at the address stored below it
 */

public class SAM_STOREIND extends SamInstruction {
	public void exec() throws SystemException {
		Data data = mem.pop();
		mem.setMem(mem.popMA(), data);
		cpu.inc(PC);
	}
}
