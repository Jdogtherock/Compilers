package edu.cornell.cs.sam.core;
import edu.cornell.cs.sam.core.instructions.Instruction;
import java.util.List;

/**
 * The program object contains the symbol
 * table for the program and the instructions
 * to execute
 */
public interface Program {
	/**
	 * Add instruction to the program
	 * @param ins The instruction to add
	 */
	public abstract void addInst(Instruction ins);
	/**  
	 * Add multiple instructions to the program
	 * @param ins An array of instructions to add
	 */
	public abstract void addInst(Instruction[] ins);
	/**
	 * Get instruction at specificed location
	 * @param pos The instruction address
	 * @return The instruction located at that address
	 */
	public abstract Instruction getInst(int pos);
	/**
	 * Get all instructions as a list 
	 * @return The list of instructions
	 */
	public abstract List<Instruction> getInstList();
	/**
	 * Gets total number of instructions in program
	 * @return The number of instructions 
	 */
	public abstract int getLength();
	/**
	 * Returns the symbol table
	 * @return The symbol table for this program
	 */
	public abstract SymbolTable getSymbolTable();
	/**
	 * Returns the references table 
	 * @return The references table for this program
	 */
	public abstract ReferenceTable getReferenceTable();
	/**
	 * Sets the symbol table
	 * @param table The symbol table for this program
	 */
	public abstract void setSymbolTable(SymbolTable table);
	/**
	 * Sets the references table
	 * @param table The references table for this program
	 */
	public abstract void setReferenceTable(ReferenceTable table);
	/** 
	 * Returns whether this program can be executed - 
	 * it is executable if all symbols have been resolved. 
	 * @return true if this program contains no unresolved references
	 */
	public abstract boolean isExecutable();

        /** 
	 * Resolves references in this program from the symbol table 
	 */
        public abstract void resolveReferences ();

        /** 
	 * Resolves prog's references from the symbol table
	 * @param prog The program whose references to resolve
	 */
        public abstract void resolveReferencesFrom(Program prog);
}
