package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/** 
 * Pops the address of a string off the stack,
 * retrieves the string, and prints it on the video interface
 */

public class SAM_WRITESTR extends SamInstruction {
	public void exec() throws SystemException {
		String str = "";
		int addr = mem.popMA();
		char ch;
		while((ch = (char) mem.getValue(addr++)) != '\0') 
			str += ch;
		if (video != null) video.writeString(str);
		cpu.inc(PC);
	};
}

