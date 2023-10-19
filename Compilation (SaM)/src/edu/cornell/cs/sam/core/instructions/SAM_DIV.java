package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Divides two integers on the stack
 */

public class SAM_DIV extends SamInstruction {
	public void exec() throws SystemException {
		int divisor = mem.popINT();
		int divided = mem.popINT();
	
		try {
			mem.pushINT(divided / divisor);
		}
		
		catch (ArithmeticException e) {
			throw new SystemException ("Attempted division by zero.");
		}
		cpu.inc(PC);
	}
}
