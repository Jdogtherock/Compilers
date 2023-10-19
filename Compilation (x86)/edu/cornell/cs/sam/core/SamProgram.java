package edu.cornell.cs.sam.core;

import edu.cornell.cs.sam.core.instructions.Instruction;
import edu.cornell.cs.sam.core.instructions.SamIntInstruction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Implementation of Program using an ArrayList for storage.
 * See Program interface for more information
 */
public class SamProgram implements Program, java.io.Serializable {
	/* Program Definition: A Sequence of valid SaM instructions, labels, and comments,
	   consisting of at least one instruction. */

	private List<Instruction> instructions = new ArrayList<Instruction>();
	private SymbolTable syms = new SamSymbolTable();
	private ReferenceTable refs = new SamReferenceTable();
	private static final String BR = System.getProperty("line.separator");

	/* Instructions */
	public Instruction getInst(int pos) {
		return instructions.get(pos);
	}

	public List<Instruction> getInstList() {
		return instructions;
	}

	public void addInst(Instruction i) {
		instructions.add(i);
	}

	public void addInst(Instruction[] arr) {
		for (int a = 0; a < arr.length; a++)
			instructions.add(arr[a]);
	}

	public int getLength() {
		return instructions.size();
	}

	/* Symbolic and Reference Tables */
	public SymbolTable getSymbolTable() {
		return syms;
	}

	public ReferenceTable getReferenceTable() {
		return refs;
	}

	public void setSymbolTable(SymbolTable table) {
		syms = table;
	}

	public void setReferenceTable(ReferenceTable table) {
		refs = table;
	}

	/* Resolve references given a symbol and a reference table.
	   Update references in the instruction list provided  */ 
	private static void resolve(SymbolTable syms, ReferenceTable refs, List<Instruction> instructions) {
		Collection<String> symbols = syms.getSymbols();
		for (String sym: symbols) {
			int address = syms.resolveAddress(sym);
			Collection<Integer> references = refs.getReferences(sym);
			if (references != null) {
				for (Integer ref: references) 
					((SamIntInstruction) instructions.get(ref)).setOperand(address);
			}
			refs.deleteSymbol(sym);
		}	
	}

	/* Resolve our own references from our symbol table */
	public void resolveReferences () {
		SamProgram.resolve(syms, refs, instructions);
	}

	/* Resolve references in prog from our symbol table */
	public void resolveReferencesFrom(Program prog) {
		SamProgram.resolve(syms, prog.getReferenceTable(), prog.getInstList());
	}

	/* Is this program executable ? */
	public boolean isExecutable() {
		return (refs.size() == 0);
	}

	/* Print everything */
	public String toString() {
		String toReturn = "Instructions: " + BR;
		for (int i = 0; i< instructions.size(); i++)
			toReturn += (i) + ": " + instructions.get(i) + BR;
		toReturn += BR + "Symbol Table:" + BR + syms.toString() + BR 
			       + "Reference Table:" + BR + refs.toString();
		return toReturn;
	}
}
