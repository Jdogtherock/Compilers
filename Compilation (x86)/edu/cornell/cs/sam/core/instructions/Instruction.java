package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Interface for an instruction to be executed
 */
public interface Instruction {
	/**
	 * Prints out the text that represents this SaM instruction
	 * @return The string representation of this instruction
	 */
	public abstract String toString();

	/**
	 * Gets the name of this instruction
	 * @return The SaM name of this instruction
	 */
	public abstract String getName();

	/**
	 * Sets the System object for this instruction
	 * @param sys The system object for this instruction
	 */
	public abstract void setSystem(Sys sys);

	/**
	 * Gets the System object of this instruction
	 * @return The system object for this instruction
	 */
	public abstract Sys getSystem();

	/**
	 * Sets the program that this instruction is part of
	 * @param prog The program containing this instruction
	 */
	public abstract void setProgram(Program prog);

	/**
	 * Gets the program that this instruction is part of
	 * @return The program containing this instruction
	 */
	public abstract Program getProgram();

	/**
	 * Executes the instruction
	 * @throws SystemException if there is a runtime error
	 */
	public abstract void exec() throws SystemException;
}
