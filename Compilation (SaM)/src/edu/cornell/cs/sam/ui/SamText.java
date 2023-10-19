package edu.cornell.cs.sam.ui;

import edu.cornell.cs.sam.core.AssemblerException;
import edu.cornell.cs.sam.core.HeapAllocator;
import edu.cornell.cs.sam.core.Memory;
import edu.cornell.cs.sam.core.SamAssembler;
import edu.cornell.cs.sam.core.Sys;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.Processor;
import edu.cornell.cs.sam.core.Program;
import edu.cornell.cs.sam.core.Video;
import edu.cornell.cs.sam.core.instructions.Instruction;
import edu.cornell.cs.sam.utils.ClassFileLoader;

import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import static edu.cornell.cs.sam.core.Memory.Type;
import static edu.cornell.cs.sam.core.HeapAllocator.Allocation;

/**
 * This is a front end for executing a SaM program from a console
 */
public class SamText implements Video {
	private final static String BR = System.getProperty("line.separator");

	public static void main(String[] args) {
		List<String> fnames = new ArrayList<String>();
		Program prg;
		SamText txt = new SamText();
		Sys sys = new Sys();
		Processor cpu = sys.cpu();
		Memory mem = sys.mem();
		cpu.init();
		mem.init();
		sys.setVideo(txt);
		int il = -1, tl = -1; //limits

		/* Argument parser */
		boolean err = false;
		for (int a = 0; a < args.length; a++) {
				
			/* Time limit */
			if (args[a].equals("+tl") && args.length > a + 1) 	
				try { tl = Integer.parseInt(args[++a]); }
				catch (NumberFormatException e) { 
					err = true; 
					break; 
				}

			/* Instruction limit */
			else if (args[a].equals("+il") && args.length > a + 1) 
				try { il = Integer.parseInt(args[++a]); }
				catch (NumberFormatException e) { 
					err = true; 
					break; 
				}

			/* Limit without argument */
			else if (args[a].equals("+il") || args[a].equals("+tl")) { 
				err = true; 
				break; 
			}
			
			else if(args[a].equals("-load")){
				String name = args[++a];
				loadInstruction(new File(name));
			}

			else if(args[a].equals("-help") || args[a].equals("--help")) {
				err = true;	
				break;	
			}

			/* Files to assemble */
			else fnames.add(args[a]);
		}

		if (err) {
			System.err.println("Usage: java SamText <options> <file1> <file2>..." + BR + 
			                   "If the options are omitted, the program runs without limits." + BR + 
			                   "If the filenames are omitted, System.in is used for input. " + BR + BR + 
			                   "Options: +tl <integer>: Time limit in milliseconds." + BR + 
			                   "         +il <integer>: Instruction limit." + BR + 
			                   "         -load: Loads the specified class file as an instruction." + BR + 
			                   "         -help: Shows this help message.");
			return;
		}

		try {
			if (fnames.size() != 0)
				prg = SamAssembler.assemble(
					fnames.toArray(new String[fnames.size()])
				);
			else {
				System.out.println("Type SAM Code, EOF to end. ");
				System.out.println("(CTRL-D on Unix, CTRL-Z on Windows)" );
				System.out.println("============================");
				prg = SamAssembler.assemble(new InputStreamReader(System.in));
			}

			System.out.println("Program assembled.");
			cpu.load(prg);
			System.out.println("Program loaded. Executing.");
			System.out.println("==========================");

			final boolean ilim = (il < 0) ? false : true; //instruction limit on?
			final boolean tlim = (tl < 0) ? false : true; //time limit on?

			/* Execute */
			if (!ilim && !tlim)
				cpu.run();
			else {
				long start = 0;
				if (tlim)
					start = System.currentTimeMillis();

				while (cpu.get(Processor.HALT) == 0) {
					cpu.step();
					if (ilim && il-- == 0)
						throw new SystemException("Program exceeded instruction limit. Terminating.");
					if (tlim && (System.currentTimeMillis() - start > tl))
						throw new SystemException("Program exceeded time limit. Terminating.");
				}
			}

			/* Exit code */
			System.out.println("Exit Status: " + mem.getMem(0));
		
			/* Check stack usage */
			if(cpu.get(Processor.SP) != 1)
				System.out.println("Warning: You do not have one item remaining on the stack");

			HeapAllocator heap = mem.getHeapAllocator();
			if (heap != null) {
				Iterator<Allocation> iter = heap.getAllocations();
				if (iter.hasNext())
					System.out.println("Warning: Your program leaks memory");
			}
		}

		catch (AssemblerException e)    { System.err.println("Assembler error: " + e); 	return; }
		catch (FileNotFoundException e) { System.err.println("File not found: " + e); 	return; }
		catch (IOException e) 		{ System.err.println("Error reading file: " + e); return; }
		catch (SystemException e) 	{ System.err.println("Stack machine error: " + e); return; }
		catch (Exception e) {
			System.err.println("Internal Error, please report to the SaM Development Group " + BR + e);
			e.printStackTrace(System.err);
			return;
		}

	}

	/* Video interface methods */
	private final static BufferedReader in = new BufferedReader(new InputStreamReader(System.in));

	public int readInt() {
		while (true) {
			try {
				System.out.print("Processor Input (enter integer): ");
				return Integer.parseInt(in.readLine());
			}
			catch (NumberFormatException e) { continue; }
			catch (IOException e) { return 0; }
		}
	}

	public String readString() {
		try {
			System.out.print("Processor Input (enter string): ");
			String s = in.readLine();
			return (s != null)? s: "";
		}
		catch (IOException e) { return ""; }
	}

	public char readChar() {
		try {
			System.out.print("Processor Input (enter character): ");
			String s = in.readLine();
			return (s != null && s.length() > 0)? s.charAt(0): 0;
		}
		catch (IOException e) { return 0; }
	}


	public float readFloat() {
		while (true) {
			try {
				System.out.print("Processor Input (enter float): ");
				return Float.parseFloat(in.readLine());
			}
			catch (NumberFormatException e) { continue; }
			catch (IOException e) { return 0; }
		}
	}

	public void writeInt(int a) { 
                System.out.println("Processor Output: " + a); 
        }

	public void writeFloat(float a) { 
                System.out.println("Processor Output: " + a); 
        }

	public void writeChar(char a) { 
                System.out.println("Processor Output: " + a); 
        }

	public void writeString(String a) { 
                System.out.println("Processor Output: " + a); 
        }

	/* Loads a new instruction into the cache */
	private static void loadInstruction(File f){
		ClassFileLoader cl = new ClassFileLoader(SamText.class.getClassLoader());
		
		String className = f.getName();
		if(className.indexOf('.') < 0){
			System.err.println("Error: Could not load instruction - improper filename.");
			System.exit(1);
		}
		if(!className.startsWith("SAM_")){
			System.err.println("Class name is missing the SAM_ prefix.");
			System.exit(1);
		}
		System.out.println("Loading Instruction...");
		className = className.substring(0, className.indexOf('.'));
		String instructionName = className.substring(4);

		try {
			Class<? extends Instruction> c = (Class<? extends Instruction>) cl.getClass(f, className);
			Instruction i = c.newInstance();
			SamAssembler.instructions.addInstruction(instructionName, c);
			System.out.println("Loaded Instruction " + instructionName);
		}
		catch (ClassCastException err) {
			System.err.println("Error: Class does not implement the Instruction interface.");
			System.exit(1);
		}			
		catch(NoClassDefFoundError err) {
			System.err.println("Error: Could not load instruction" + BR +
				"Check that it is marked public, and does not belong to any package.");
			System.exit(1);
		}
		catch(ClassNotFoundException err){
			System.err.println("Error: Could not load instruction" + BR +
				"Check that it is marked public, and does not belong to any package.");
			System.exit(1);
		}

		catch(InstantiationException err){
			System.err.println("Error: Could not load instruction" + BR +
				"Check that it is marked public, and does not belong to any package.");
			System.exit(1);
		}

		catch(IllegalAccessException err){
			System.err.println("Error: Could not load instruction" + BR +	
				"Check that it is marked public, and does not belong to any package.");
			System.exit(1);
		}
	}
}
