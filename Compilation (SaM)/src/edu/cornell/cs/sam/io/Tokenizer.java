package edu.cornell.cs.sam.io;

/* Tokenizer interface. */

public interface Tokenizer {

	/**
	 * Represents the type of a tokenizer token
	 */
	
	public enum TokenType { 

		/** 
		 * INT: a sequence of digits with only digits
		 * after the first digit. An integer will always be >= 0.
		 */

		INTEGER, 

		/** 
		 * FLOAT: a sequence of digits starting with either a period,
		 * or a digit and containing only digits and one period after
		 * the first digit. A float will always be >= 0.
		 */

		FLOAT, 

		/**
		 * WORD: a letter followed by a sequence of alphanumeric characters
		 * or underscores (_) without any whitespace. If a string such as 2.0.0
		 * is detected, this should also be considered a word. 
		 */

		WORD, 

		/** 
		 * STRING: If string processing is turned on, a word is anything 
		 * between two quotation signs. Valid escapes (\n, \r, \\, \", etc..) 
		 * should also be evaluated in this case. 
		 */

		STRING, 

		/** 
		 * CHARACTER: A single character between two apostrophes.
		 * Valid escapes are allowed. 
		 */
		
		CHARACTER,

		/**
		 * OPERATOR: Any non-alphanumeric character
		 */
		
		OPERATOR,

		/**
		 * COMMENT: The text following // on a single line
		 */

		COMMENT,

		/** 
		 * EOF: The end of file
		 */
		
		EOF; 
	}	
		
	/**
	 * Returns the type of the next token
	 * @return the type of the next token
	 */
	public abstract TokenType peekAtKind();

	/**
	 * Returns the next token (unless it is not an integer)
	 * @return the integer expected
	 * @throws TokenizerException if the next token is not an integer
	 */
	public abstract int getInt() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not a float)
	 * @return the floating point number expected
	 * @throws TokenizerException if the next token is not a float
	 */
	public abstract float getFloat() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not a word)
	 * @return the word expected
	 * @throws TokenizerException if the next token is not a word
	 */
	public abstract String getWord() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not a string)
	 * @return the string expected
	 * @throws TokenizerException if the next token is not a string
	 */
	public abstract String getString() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not a character)
	 * @return the character expected
	 * @throws TokenizerException if the next token is not a character
	 */
	public abstract char getCharacter() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not an operator)
	 * @return the operator expected
	 * @throws TokenizerException if the next token is not an operator
	 */
	public abstract char getOp() throws TokenizerException;

	/**
	 * Returns the next token (unless it is not a comment)
	 * @return the comment expected
	 * @throws TokenizerException if the next token is not a comment
	 */
	public abstract String getComment() throws TokenizerException;

	/**
	 * Removes the next token if it is an operator, and 
	 * matches the given character. Otherwise, throws TokenizerException.
	 * @param c the character to match
	 * @throws TokenizerException if the next token is not c, or is not an operator token.
	 */
	public abstract void match(char c) throws TokenizerException;

	/**
	 * Removes the next token if it is a word, and
	 * matches the given string. Otherwise, throws TokenizerException.
	 * @param s the string to match
	 * @throws TokenizerException if the next token is not s, or is not a word token
	 */
	public abstract void match(String s) throws TokenizerException;

	/**
	 * Removes the next token if it is an operator, and matches
 	 * the character c. Otherwise, returns false.
	 * @param c the character to match
	 * @return true if the next token is an operator and matches c, false otherwise
	 */
	public abstract boolean check(char c);

	/**
	 * Removes the next token if it is a word, and matches
	 * the string s. Otherwise, returns false.
	 * @param s the string to match
	 * @return true if the next token is a word, and matches s, false otherwise
	 */
	public abstract boolean check(String s);

	/**
	 * Checks if the next token is an operator and matches 
	 * the character c. Otherwise, returns false. The token is not removed.
	 * @param c the character to match
	 * @return true if the next token is an operator, and matches c, false otherwise
	 */
	public abstract boolean test(char c);

	/**
	 * Checks if the next token is a word and matches 
	 * the string s. The token is not removed.
	 * @param s the string to match 
	 * @return true if the next token is a word, and matches s, false otherwise
	 */
	public abstract boolean test(String s);

	/**
	 * Pushes the last token requested back. Implementations
	 * are required to be able to push back to the start of the file.
	 */
	public abstract void pushBack();

	/**
	 * Checks if the stream can be pushed back
	 * @return true if it can be pushed back, false if at the beginning of the file
	 */
	public abstract boolean canPushBack();

	/**
	 * Skips the next token
	 */
	public abstract void skipToken();

	/**
	 * Returns the whitespace in the file before the current token. This may be null
	 * @return the whitespace string
	 */
	public abstract String getWhitespaceBeforeToken();

	/**
	 * Returns the line number of the last token requested
	 * @return the line number of the last token
	 */
	public abstract int lineNo();

	/**
	 * Returns the line number of the next token to be read
	 * @return the line number of the next token
	 */
	public abstract int nextLineNo();

	/**
	 * Closes the input stream
	 */
	public abstract void close();
}
