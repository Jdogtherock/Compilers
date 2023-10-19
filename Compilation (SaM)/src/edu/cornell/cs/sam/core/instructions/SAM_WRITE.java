package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pops an integer off the stack, and writes it to the video interface
 */

public class SAM_WRITE extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			video.writeInt(mem.popINT());
		else
			mem.popINT();
		cpu.inc(PC);
	}
}


