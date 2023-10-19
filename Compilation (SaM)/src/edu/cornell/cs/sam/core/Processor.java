package edu.cornell.cs.sam.core;

/**
 * The processor is responsible for stepping through 
 * instructions, and manipulating the stack and registers.
 */
public interface Processor {
	/**
	 * Loads the program that the processor must execute
	 * @param prog The program to execute
	 * @throws SystemException if the program was not loaded successfully 
	 */
	public abstract void load(Program prog) throws SystemException;
	
	/**
	 * Returns the program that is currently in memory to be executed
	 * @return The program currently executing
	 */
	public abstract Program getProgram();

	/**
	 * Returns the system object of this processor
	 * @return The system object for the processor
	 */
	public abstract Sys getSystem();

	/**
	 * Executes one instruction. If that instruction is HALT,
	 * the next invocation of step() without resetting the processor
	 * will throw an exception.
	 * @throws SystemException if there is an error during execution
	 */
	public abstract void step() throws SystemException;

	/**
	 * Execute the whole program. This function
	 * will block until the full program has executed,
	 * @throws SystemException if there is an error during execution
	 */
	public abstract void run() throws SystemException;

	/** 
	 * The program counter - contains the address of the instruction to be executed next
	 */
	public final static int PC = 0;

	/**
	 * The stack pointer - points to the first free location on the stack
	 */
	public final static int SP = 1;

	/**
	 * The frame base register - points to the base of the current function frame
	 */
	public final static int FBR = 2;

	/**
	 * The halt register - set to one to stop the processor
	 */
	public final static int HALT = 3;

	/**
	 * Initializes all registers to their initial state
	 */
	public abstract void init();

	/**
	 * Returns the register value requested
	 * @param register The register to query
	 * @return The value of the queries register
	 */
	public abstract int get(int register);

	/**
	 * Returns a new copy of the register array.
	 * @return The array of registers
	 */
	public abstract int[] getRegisters();

	/**
	 * Set value of the register
	 * @param register The register to set
	 * @param value The value to which to set the register
	 * @throws SystemException if the register value is invalid
	 */
	public abstract void set(int register, int value) throws SystemException;

	/**
	 * Increments the register and returns the new value.
	 * @param register The register to increment
	 * @return The incremented register
	 * @throws SystemException if the resulting register value is invalid
	 */
	public abstract int inc(int register) throws SystemException;

	/**
	 * Decrements the register and returns the new value.
	 * @param register The register to decrement
	 * @return The decremented register
	 * @throws SystemException if the resulting register value is invalid
	 */
	public abstract int dec(int register) throws SystemException;

	/**
	 * Verifies if the value is valid for this
	 * registers and throws an exception if not.
	 * This is done internally as well, but this 
	 * function is exposed to force a SystemException
	 * earlier.
	 * @param register The register to verify
	 * @param value The value to verify
	 * @throws SystemException if the value is invalid for this register 
	 */
	public abstract void verify(int register, int value) throws SystemException;
}
