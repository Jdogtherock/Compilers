package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Reads a character from the video interface,
 * and pushes it on the stack
 */

public class SAM_READCH extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			mem.pushCH(video.readChar());
		else
			mem.pushCH('\0');
		cpu.inc(PC);
	}
}


