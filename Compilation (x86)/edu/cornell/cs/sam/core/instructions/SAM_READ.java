package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Reads an integer from the video interface,
 * and pushes that on the stack 
 */

public class SAM_READ extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			mem.pushINT(video.readInt());
		else
			mem.pushINT(0);
		cpu.inc(PC);
	}
}


