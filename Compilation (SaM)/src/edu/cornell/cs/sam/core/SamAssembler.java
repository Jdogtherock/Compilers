package edu.cornell.cs.sam.core;

import edu.cornell.cs.sam.core.instructions.Instruction;
import edu.cornell.cs.sam.core.instructions.SamAddressInstruction;
import edu.cornell.cs.sam.core.instructions.SamCharInstruction;
import edu.cornell.cs.sam.core.instructions.SamFloatInstruction;
import edu.cornell.cs.sam.core.instructions.SamInstruction;
import edu.cornell.cs.sam.core.instructions.SamIntInstruction;
import edu.cornell.cs.sam.core.instructions.SamStringInstruction;
import edu.cornell.cs.sam.io.Tokenizer;
import edu.cornell.cs.sam.io.TokenParseException;
import static edu.cornell.cs.sam.io.Tokenizer.TokenType;
import edu.cornell.cs.sam.io.SamTokenizer;
import static edu.cornell.cs.sam.io.SamTokenizer.TokenizerOptions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;

import java.util.HashMap;

/**
 * The SamAssembler is responsible for reading in
 * a file and creating a Program object that can
 * then be sent to the Processor for execution
 */
public class SamAssembler {
	private static final String pkgName = SamInstruction.class.getPackage().getName();
	
	public static SamInstructionCache instructions = new SamInstructionCache();

	/**
	 *  Assembles a program from a single file
	 */
	public static Program assemble(String filename) throws
			AssemblerException, FileNotFoundException, IOException {
		System.out.println(filename);
		return assemble(new BufferedReader(new FileReader(filename)));
	}

	/**
	 * Assembles a program from a single reader
	 */
	public static Program assemble(Reader r) throws
			AssemblerException, IOException {
		Program prog = new SamProgram();
		prog = parse(r, prog);

		if (prog.getLength() == 0)
			throw new AssemblerException("Cannot assemble null program.");

                prog.resolveReferences();

		return prog;
	}

	/**
	 * Assembles a program from multiple filenames
	 */
	public static Program assemble(String[] filenames) throws AssemblerException,
		FileNotFoundException, IOException {

		Program prog = new SamProgram();

		for (String fname: filenames) {
			Reader reader = new BufferedReader(new FileReader(fname));
			prog = parse (reader, prog);
			/* FIXME: Identify file on error */
		}

		if (prog.getLength() == 0)
			throw new AssemblerException("Cannot assemble null program.");

		prog.resolveReferences();

		return prog;
	}

	private static Program parse(Reader input, Program prog) 
			throws AssemblerException, IOException {
			
		Tokenizer in;
		
		try {	
	
			in = new SamTokenizer(input,
				TokenizerOptions.PROCESS_STRINGS,
				TokenizerOptions.PROCESS_CHARACTERS); 
		} 
		catch (TokenParseException e) {
			throw new AssemblerException("Unable to create token stack: " + e.getMessage(), e.getLine());	
		} 

		SymbolTable ST = prog.getSymbolTable();
		ReferenceTable RT = prog.getReferenceTable();

		do {
			String str = null;

			/* ====================LABELS================================== */
			String label;
			while ((label = extractLabel(in)) != null) {
				/* Catch Duplicates */
				if (ST.resolveAddress(label) >= 0)
					throw new AssemblerException("Duplicate Label", in.lineNo());

				/* Add to the symbol table */
				ST.add(label, prog.getLength());
			}
			
			if(in.peekAtKind() != TokenType.WORD)
				throw new AssemblerException("Expected instruction", in.nextLineNo());
			str = in.getWord();
	
			/*==================== INSTRUCTIONS: NAME =======================*/
			/* At this point, str should contain the instruction name */
			Instruction i;

			try  { 
				i = instructions.getInstruction(str); 
			}
			catch (IllegalAccessException e)  { 
				throw new AssemblerException("Unknown Instruction: " + str, in.lineNo()); 
			}
			catch (InstantiationException e)  { 
				throw new AssemblerException("Unknown Instruction: " + str, in.lineNo()); 
			}
			catch (ClassNotFoundException e)  { 
				throw new AssemblerException("Unknown Instruction: " + str, in.lineNo()); 
			}
			i.setProgram(prog);

			/*==================== INTEGER INSTRUCTIONS: OPERAND ============*/
			/*__________Operand could be either a label or an integer._______*/

			if (i instanceof SamIntInstruction) {

				boolean addr_ins = i instanceof SamAddressInstruction;

				/* Store symbolic reference */
				if (addr_ins && (in.peekAtKind() == TokenType.WORD))	
					RT.add(in.getWord(), prog.getLength());

				/* Store symbolic reference */
				else if (addr_ins && (in.peekAtKind() == TokenType.STRING)) 
					RT.add(in.getString(), prog.getLength());

				/* Negative integers */
				else if(in.peekAtKind() == TokenType.OPERATOR){
					if(in.getOp() != '-' || in.peekAtKind() != TokenType.INTEGER)
						throw new AssemblerException("Instruction " + str + 
                                		" requires an integer operand", in.lineNo());
					((SamIntInstruction)i).setOperand(- in.getInt());
				}

				/* Positive integers */
				else if(in.peekAtKind() == TokenType.INTEGER)
					 ((SamIntInstruction)i).setOperand(in.getInt());

				/* Otherwise, crash */
				else throw new AssemblerException("Instruction " + str + 
                                        " requires an integer operand", in.nextLineNo());
			}

			/*===================== FLOAT INSTRUCTIONS: OPERAND =============*/
			else if (i instanceof SamFloatInstruction){
				if(in.peekAtKind() == TokenType.FLOAT)
					((SamFloatInstruction)i).setOperand(in.getFloat());
				else if(in.peekAtKind() == TokenType.OPERATOR){
					if(in.getOp() != '-' || in.peekAtKind() != TokenType.FLOAT)
						throw new AssemblerException("Instruction " + str + 
                                		" requires a float operand", in.lineNo());
					((SamFloatInstruction)i).setOperand(- in.getFloat());
				}
				else throw new AssemblerException("Instruction " + str + 
                                        " requires a float operand", in.nextLineNo());
			}

			/*===================== CHAR INSTRUCTIONS: OPERAND  =============*/
			else if (i instanceof SamCharInstruction)
				if(in.peekAtKind() == TokenType.CHARACTER)
					((SamCharInstruction)i).setOperand(in.getCharacter());
				else throw new AssemblerException("Instruction " + str + 
                                        " requires a character operand", in.nextLineNo());

			/*===================== STRING INSTRUCTIONS: OPERAND ============*/
			else if (i instanceof SamStringInstruction){
				if(in.peekAtKind() == TokenType.STRING)
					((SamStringInstruction)i).setOperand(in.getString());
				else throw new AssemblerException("Instruction " + str + 
                                        " requires a string operand", in.nextLineNo());
			}

			/*===================== ADD INSTRUCTION TO PROGRAM ===============*/
			prog.addInst(i);

		} while (in.peekAtKind() != TokenType.EOF);

		/* Close Input strem */
		in.close();

		return prog;
	}

	/**
	 * Returns the next label, or null if no label is found
	 */
	private static String extractLabel(Tokenizer in){
		String s = null;
		if (in.peekAtKind() == TokenType.WORD)
			s = in.getWord();
		else if (in.peekAtKind() == TokenType.STRING)
			s = in.getString();
		else
			return null;

		if(!in.check(':')){
			in.pushBack();
			return null;
		}

		return s;
	}
	
	public static class SamInstructionCache{
		private HashMap <String, Class<? extends Instruction>> instructions = 
			new HashMap <String, Class<? extends Instruction>> ();
		
		public Instruction getInstruction(String s) throws IllegalAccessException, 
					InstantiationException, ClassNotFoundException{
			Instruction i;
			if(instructions.containsKey(s)){
				i = instructions.get(s).newInstance();
				return i;
			}
			
			Class<? extends Instruction> c = (Class<? extends Instruction>)
				(Class.forName(SamAssembler.pkgName + "." + "SAM_" + s));
			addInstruction(s, c);
			return c.newInstance();
		}
		
		public void addInstruction(String s, Class<? extends Instruction> c){
			instructions.put(s, c);
		}
	}

}

