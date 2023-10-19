package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Pops a floating point number off the stack
 * and writes it to the video interface
 */

public class SAM_WRITEF extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			video.writeFloat(mem.popFLOAT());
		else
			mem.popFLOAT();
		cpu.inc(PC);
	}
}
