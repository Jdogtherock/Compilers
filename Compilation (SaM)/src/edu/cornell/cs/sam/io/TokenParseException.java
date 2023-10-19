package edu.cornell.cs.sam.io;

/**
 * Exception that indicates error while parsing a token
 */
public class TokenParseException extends TokenizerException {

	public TokenParseException(String msg, int lc) {
		super(msg, lc);
	}
}

