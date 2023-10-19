package edu.cornell.cs.sam.core.instructions;

/**
 * All instructions with a String operand derive from this class
 */

public abstract class SamStringInstruction extends SamInstruction {
	protected String op;

	public String toString() {
		return name + " " + '\"' + op + '\"';
	}

	/** 
	 * Get the String operand of this instruction
	 * @return The String operand
	 */
	public String getOperand() {
		return op;
	}

	/**
	 * Set the String operand of this instruction
	 * @param operand The String operand 
	 */
	public void setOperand(String operand) {
		op = operand;
	}
}

