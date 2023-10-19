package edu.cornell.cs.sam.core;

/**
 * The Video interface allows the processor
 * to output data or to request input. If a video implementation
 * is not provided to the processor, SaM READ commands
 * will place a 0 on the stack, and WRITE commands will
 * not output any data
 */
public interface Video {

	/**
	 * Writes integer output to the video implementation
	 * @param i The integer to be written
	 */
	public abstract void writeInt(int i);

	/**
	 * Writes string output to the video implementation
	 * @param str The string to be written
	 */
	public abstract void writeString(String str);

	/**
	 * Writes floating point output to the video implementation
	 * @param f The float to be written
	 */
	public abstract void writeFloat(float f);

	/**
	 * Writes character output to the video implementation
	 * @param c The character to be written
	 */
	public abstract void writeChar(char c);

	/**
	 * Requests integer input from the video implementation
	 * @return The integer received
	 */
	public abstract int readInt();

	/**
	 * Request floating point input from the video implementation
	 * @return The floating point number received
	 */
	public abstract float readFloat();

	/**
	 * Request character input from the video implementation
	 * @return The character received
	 */
	public abstract char readChar();

	/**
	 * Request String input from the video implementation
	 * @return The String received
	 */
	public abstract String readString();
}
