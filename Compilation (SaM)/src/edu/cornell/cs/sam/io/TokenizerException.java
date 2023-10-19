package edu.cornell.cs.sam.io;

/**
 * Used for errors with the tokenizer
 */
public class TokenizerException extends RuntimeException {

	private String message = "Tokenizer Exception";
	private int line = -1;

	/*  Constructors */
	public TokenizerException() {}
	public TokenizerException(String msg) { message = msg; }
	public TokenizerException(int lc) { line = lc; }
	public TokenizerException(String msg, int lc) {
		message = msg;
		line = lc;
	}

	/* Error Retrieval */
	public String getMessage() { return message; }
	public int getLine() { return line; }
	public String toString() 
		{ return (line > -1) ? (message + ": line " + line) : message; }
}
