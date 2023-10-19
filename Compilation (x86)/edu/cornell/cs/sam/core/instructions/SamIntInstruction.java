package edu.cornell.cs.sam.core.instructions;

/**
 * All instructions with an integer operand are derived from this class.
 */

public abstract class SamIntInstruction extends SamInstruction {
	protected int op;

	public String toString() {
		return name + " " + op;
	}

	/**
	 * Returns the integer operand of this instruction 
	 * @return The integer operand
	 */

	public int getOperand() {
		return op;
	}

	/**
	 * Sets the integer operand of this instruction
	 * @param operand The integer operand
	 */

	public void setOperand(int operand) {
		op = operand;
	}
}
