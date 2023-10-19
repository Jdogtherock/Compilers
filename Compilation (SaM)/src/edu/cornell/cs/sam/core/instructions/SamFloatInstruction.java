package edu.cornell.cs.sam.core.instructions;

/**
 * All instructions with a floating point operand derive from this class
 */

public abstract class SamFloatInstruction extends SamInstruction {
	protected float op;

	public String toString() {
		return name + " " + op;
	}

	/**
	 * Get the floating point operand of this instruction
	 * @return The floating point operand
	 */

	public float getOperand() {
		return op;
	}

	/**
	 * Set the floating point operand of this instruction
	 * @param operand The floating point operand
	 */

	public void setOperand(float operand) {
		op = operand;
	}
}
