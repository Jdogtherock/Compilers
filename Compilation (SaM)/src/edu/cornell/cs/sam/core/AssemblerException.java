package edu.cornell.cs.sam.core;

/**
 * Specifies that there was an error while the assembler
 * was loading the file
 */
public class AssemblerException extends Exception {

	private String message = "Assembler Exception";
	private int line = -1;

	/*  Constructors */
	public AssemblerException() {}

	public AssemblerException(String msg) { 
                message = msg; 
        }

	public AssemblerException(int lc) { 
                line = lc; 
        }

	public AssemblerException(String msg, int lc) {
		message = msg;
		line = lc;
	}

	/* Error Retrieval */
	/**
	 * Retrieves the message
	 */
	public String getMessage() { 
                return message; 
        }

	/**
	 * Retrives the line number if provided, -1 otherwise
	 */
	public int getLine() { 
                return line; 
        }

	public String toString() { 
                return (line > -1) ? (message + ": line " + line) : message; 
        }
}
