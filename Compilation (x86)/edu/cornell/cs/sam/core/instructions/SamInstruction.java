package edu.cornell.cs.sam.core.instructions;
import java.io.Serializable;
import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.Processor;
import edu.cornell.cs.sam.core.Memory;
import edu.cornell.cs.sam.core.Video;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.SystemException;
import static edu.cornell.cs.sam.core.Memory.Type;

/**
 * Base class for all SamInstructions
 * Defines common features such as shortcut variables and methods, 
 * toString(), name, cpu/mem access, and an abstract
 * exec(). 
 */
public abstract class SamInstruction implements Instruction, Serializable {

	/* Identity/String representation of this instruction */
	private final Package pkg = this.getClass().getPackage();
	private final String prefix = "SAM_";
 	protected final String name = 
		this.getClass().getName().substring(
			(pkg == null)? 
				prefix.length() :
				pkg.getName().length() + 1 + prefix.length()
		);

	public String toString() {
		return name;
	}

	public String getName() {
		return name;
	}

	/* Access to hardware components */
	protected transient Program prog;
	protected transient Processor cpu = null;
	protected transient Memory mem = null;
	protected transient Video video = null;
	protected transient Sys sys;

	/* Set/get methods */
	public void setSystem(Sys sys) {
		this.sys = sys;
		cpu = sys.cpu();
		mem = sys.mem();
		video = sys.video();
	}

	public Sys getSystem() {
		return sys;
	}

	public void setProgram(Program p) {
		prog = p;
	}

	public Program getProgram() {
		return prog;
	}

	protected final static int 
		PC = Processor.PC, 
		SP = Processor.SP, 
		HALT = Processor.HALT, 
		FBR = Processor.FBR;

	public abstract void exec() throws SystemException;
}
