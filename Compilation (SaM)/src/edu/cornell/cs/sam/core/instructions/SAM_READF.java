package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Reads a floating point number from 
 * the video interface, and pushes that on the stack
 */

public class SAM_READF extends SamInstruction {
	public void exec() throws SystemException {
		if (video != null)
			mem.pushFLOAT(video.readFloat());
		else
			mem.pushFLOAT(0.0f);
		cpu.inc(PC);
	}
}


