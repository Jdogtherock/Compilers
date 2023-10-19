package edu.cornell.cs.sam.core.instructions;

/**
 * All instructions with a program address operand derive from this class.
 */

public abstract class SamAddressInstruction extends SamIntInstruction {
	public String toString() {
		String label = null;

		if (prog != null) 
			label = prog.getSymbolTable().resolveSymbol(op);

		return name + " " + ((label == null) ? Integer.toString(op) : label);
	}
}


