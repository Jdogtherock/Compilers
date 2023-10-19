package edu.cornell.cs.sam.core.instructions;

/**
 * All instructions with a character operand derive from this class
 */

public abstract class SamCharInstruction extends SamInstruction {
	protected char op;

	public String toString() {
		return name + " " + '\'' + op + '\'';
	}

	/**
	 * Get the character operand of this instruction
	 * @return The character operand
	 */

	public char getOperand() {
		return op;
	}

	/** 
	 * Set the character operand of this instruction
	 * @param operand The character operand
	 */

	public void setOperand(char operand) {
		op = operand;
	}
}
