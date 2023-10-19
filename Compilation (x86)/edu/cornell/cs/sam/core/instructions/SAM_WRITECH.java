package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pops a character off the stack, and writes
 * it to the video interface
 */

public class SAM_WRITECH extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			video.writeChar(mem.popCH());
		else
			mem.popCH();
		cpu.inc(PC);
	}
}
