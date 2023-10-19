package edu.cornell.cs.sam.core;
/**
 * Indicates there was a System error (cpu/memory error) 
 * during execution
 */
public class SystemException extends Exception {

	private String message = "Stack Machine Exception";

	/*  Constructors */
	public SystemException() {}

	public SystemException(String msg) { 
                message = msg; 
        }

	/* Error Retrieval */
	public String toString() {
		return message;
	}
}
