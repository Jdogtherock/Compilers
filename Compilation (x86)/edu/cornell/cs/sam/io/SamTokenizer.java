package edu.cornell.cs.sam.io;

import java.io.InputStreamReader;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.PushbackReader;
import java.io.Reader;

import java.util.Stack;

/**
 * This is a SamTokenizer implementation that
 * has full backwards/forwards mobility and loads everything into
 * memory as soon as it is created. By default, quote processing
 * is disabled and comment ignoring is enabled. You may use
 * one of the constructors with options to enabled/disable this options
 */
public class SamTokenizer implements Tokenizer {

	public enum TokenizerOptions { 
		PROCESS_COMMENTS, 
		PROCESS_STRINGS, 
		PROCESS_CHARACTERS; 
	}

	/* The source */
	private PushbackReader in;

	/* The read and unread tokens */
	private Stack<Token> tokens = new Stack<Token> ();
	private Stack<Token> readTokens = new Stack<Token> ();

	/* Default settings */
	private boolean processStrings = false;
	private boolean processCharacters = false;
	private boolean processComments = false;

	/**
	 * Creates a new SamTokenizer with a file for a source
	 * @param FileName The file name of the file to read
	 * @param opt Options for parsing
	 * @throws IOException If there is a file error
	 * @throws FileNotFoundException If the file could not be found
	 */
	public SamTokenizer(String FileName, TokenizerOptions... opt)
			throws IOException, FileNotFoundException, TokenParseException {
		FileReader r = new FileReader(FileName);
		parseOptions(opt);
		in = new PushbackReader(r);
		read();
	}

	/**
	 * Creates a new SamTokenizer with a Reader as a source
	 * @param r The source
	 * @param opt Options for parsing
	 * @throws IOException if there is a stream error
	 */
	public SamTokenizer(Reader r, TokenizerOptions... opt) 
			throws IOException, TokenParseException {
		in = new PushbackReader(r);
		parseOptions(opt);
		read();
	}

	/**
	 * Creates a new SamTokenizer with System.in as a source
	 * @param opt Options for parsing
	 * @throws IOException if there is a stream error
	 */
	public SamTokenizer(TokenizerOptions... opt) 
			throws IOException, TokenParseException {
		in = new PushbackReader(new InputStreamReader(System.in));
		parseOptions(opt);
		read();
	}
	
	/* Parses the options and puts them into action */
	private void parseOptions(TokenizerOptions... opt){
		for(int i = 0; i < opt.length; i++){
			switch(opt[i]){
				case PROCESS_COMMENTS:
					processComments = true;
					break;
				case PROCESS_STRINGS:
					processStrings = true;
					break;
				case PROCESS_CHARACTERS:
					processCharacters = true;
					break;
			}
		}
	}

	/* Reads and parses the stream into tokens */
	private void read() throws IOException, TokenParseException {
		int lineNo = 1;
		int cin;
		char c;
		String whitespace = "";
		while ((cin = in.read()) != -1) {
			c = (char)cin;
			if (c == '\n')
				lineNo++;

			/* Now we need to detect if its a word, operator, or integer */
			if (Character.isWhitespace(c)){
				whitespace += c;
				continue;
			}

			/* Skip out the comments */
			else if (c == '/') {
				int din = in.read();
				if (din == -1){
					tokens.push(new OperatorToken('/', whitespace, lineNo));
					whitespace = "";
				}

				/* This is a valid character but not a comment */
				else if ((char)din != '/') {
					in.unread(din);
					tokens.push(new OperatorToken('/', whitespace, lineNo));
					whitespace = "";
				}
				else {
					/* OK, so this is a comment */
					String comment = "";
					while ((din = in.read()) != -1){
						comment += din;
						if (din == '\n'){
							in.unread(din);
							break;
						}
					}
					if(processComments) {
						tokens.push(new CommentToken(comment, whitespace, lineNo));
						whitespace = "";
					}						
				}
			}

			/* Strings are anything between two " marks */
			else if (processStrings && c == '"') {
				String str = "";
				while ((cin = in.read()) != -1 && cin != '"')
					str += readChar(cin, lineNo);
				if(cin == -1) 
					throw new TokenParseException("Incomplete string token - missing \"", lineNo);
				tokens.push(new StringToken(str, whitespace, lineNo));
				whitespace = "";
			}

			/* Characters are like strings except */
			else if (processCharacters && c == '\'') {
				cin = in.read();
				if(cin == -1) 
					throw new TokenParseException("Incomplete character token - missing \'", lineNo);
				char a = readChar(cin, lineNo);
				if(in.read() != '\'')
					throw new TokenParseException("Character token is too long", lineNo);
				tokens.push(new CharToken(a, whitespace, lineNo));
				whitespace = "";
			}

			/* now it's either an integer or a word or an operator */
			else if (Character.isLetter(c)) {

				/* It's a word */
				String word = "";
				word += c;
				int din;
				while ((din = in.read()) != -1) {
					if (Character.isLetter((char)din) || Character.isDigit((char)din) || din == '_')
						word += (char)din;
					else {
						in.unread(din);
						break;
					}
				}
				tokens.push(new WordToken(word, whitespace, lineNo));
				whitespace = "";
			}
			else if (Character.isDigit(c)) {
				in.unread(c);
				readNumber(lineNo, whitespace, null);
				whitespace = "";
			}
			else {
				/* Now this is an operator. The only problem . can also be a number
				   So check for that */
				if (c == '.') {
					int din = in.read();
					if (din == -1)
						tokens.push(new OperatorToken(c, whitespace, lineNo));
					else if (Character.isDigit((char)din)) {
						in.unread(din);
						readNumber(lineNo, whitespace, ".");
						whitespace = "";
					}
					else {
						in.unread(din);
						tokens.push(new OperatorToken(c, whitespace, lineNo));
						whitespace = "";
					}
				}
				else {
					tokens.push(new OperatorToken(c, whitespace, lineNo));
					whitespace = "";
				}
			}
		}
		/* push the EOF token onto the stack */
		tokens.push(new EOFToken(whitespace, lineNo));

		/* Reverse the stack */
		Stack<Token> a = new Stack<Token> ();
		while (!tokens.empty())
			a.push(tokens.pop());
		tokens = a;
	}

	/* Reads in a number - tok is the start of the number */
	private void readNumber(int lineNo, String whitespace, String tok) 
			throws IOException, TokenParseException {
		TokenType type = TokenType.INTEGER;
		if (tok == null)
			tok = "";
		else if (tok.equals("."))
			type = TokenType.FLOAT;
		int din;
		while ((din = in.read()) != -1) {
			if (din == '.') {
				switch (type) {
					case INTEGER :
						type = TokenType.FLOAT;
						break;
					case FLOAT :
						type = TokenType.WORD;
						break;
				}
				tok += (char)din;
			}
			else if (Character.isDigit((char)din) || (type == TokenType.WORD 
				&& (Character.isLetter((char)din) || din == '_')))
				tok += (char)din;
			else {
				in.unread(din);
				break;
			}
		}
		try {
			switch (type) {
				case FLOAT :
					tokens.push(new FloatToken(Float.parseFloat(tok), whitespace, lineNo));
					break;
				case INTEGER :
					tokens.push(new IntToken(Integer.parseInt(tok), whitespace, lineNo));
					break;
				default :
					tokens.push(new WordToken(tok, whitespace, lineNo));
			}
		}
		catch (NumberFormatException e) {
			throw new TokenParseException("Unparsable number " + tok, lineNo);
		}
	}
	
	/* Reads in a character and returns it, parsing escapes if necessary */
	private char readChar(int cin, int lineNo) throws IOException, TokenParseException {
		if (cin == '\\') {
			cin = in.read();
			if(Character.isDigit(cin)){
				String codeS = "" + (char)cin;
				while(Character.isDigit(cin = in.read())){
					codeS += (char) cin;
					if(codeS.length() > 3)
						throw new TokenParseException("Invalid escape code " + codeS, lineNo);
				}
				in.unread(cin);
				int code;
				try{ code = Integer.parseInt(codeS); }
				catch(NumberFormatException e){
					throw new TokenParseException("Unparsable number " + codeS, lineNo);
				}
				if(code < 0 || code > 255)
					throw new TokenParseException("Invalid escape code " + code, lineNo);
				return (char)code;
			}
			else{
				switch (cin) {
					case 't' :
						return '\t';
					case '\\' :
						return '\\';
					case 'n' :
						return '\n';
					case 'r' :
						return '\r';
					case '"' :
						return '"';
					case '\'':
						return '\'';
					default :
						throw new TokenParseException("Invalid escape character \'" + 
								cin + "\'", lineNo);
				}
			}
		}
		else
			return (char)cin;
	}


	/* Return the type of the next token */
	public TokenType peekAtKind() {
		if (tokens.empty())
			return TokenType.EOF;
		return tokens.peek().getType();
	}

	/* Get INT token */
	public int getInt() throws TokenizerException {
		if (peekAtKind() == TokenType.INTEGER) {
			IntToken i = (IntToken)tokens.pop();
			readTokens.push(i);
			return i.getInt();
		}
		else
			throw new TokenizerException("Attempt to read non-integer value as an integer", lineNo());
	}

	/* Get FLOAT token */
	public float getFloat() throws TokenizerException {
		if (peekAtKind() == TokenType.FLOAT) {
			FloatToken f = (FloatToken)tokens.pop();
			readTokens.push(f);
			return f.getFloat();
		}
		else
			throw new TokenizerException("Attempt to read non-float value as a float", lineNo());
	}

	/* Get WORD token */
	public String getWord() throws TokenizerException {
		if (peekAtKind() == TokenType.WORD) {
			WordToken word = (WordToken)tokens.pop();
			readTokens.push(word);
			return word.getWord();
		}
		else
			throw new TokenizerException("Attempt to read non-word value as a word.", lineNo());
	}

	/* Get STRING token */
	public String getString() throws TokenizerException {
		if (peekAtKind() == TokenType.STRING) {
			StringToken str = (StringToken)tokens.pop();
			readTokens.push(str);
			return str.getString();
		}
		else
			throw new TokenizerException("Attempt to read non-string value as a string.", lineNo());
	}
	
	/* Get CHARACTER token */
	public char getCharacter() throws TokenizerException {
		if (peekAtKind() == TokenType.CHARACTER) {
			CharToken c = (CharToken)tokens.pop();
			readTokens.push(c);
			return c.getChar();
		}
		else
			throw new TokenizerException("Attempt to read non-char value as a char.", lineNo());
	}
	
	/* Get OPERATOR token */
	public char getOp() throws TokenizerException {
		if (peekAtKind() == TokenType.OPERATOR) {
			OperatorToken op = (OperatorToken)tokens.pop();
			readTokens.push(op);
			return op.getOp();
		}
		else
			throw new TokenizerException("Attempt to read non-operator value as an op", lineNo());
	}

	/* Get COMMENT token */
	public String getComment() throws TokenizerException {
		if (peekAtKind() == TokenType.COMMENT) {
			CommentToken c = (CommentToken)tokens.pop();
			readTokens.push(c);
			return c.getComment();
		}
		else
			throw new TokenizerException("Attempt to read non-comment value as a comment", lineNo());
	}

	/* Eat an operator, or throw an exception */
	public void match(char c) throws TokenizerException {
		char n;
		if (peekAtKind() == TokenType.OPERATOR) {
			n = getOp();
			if (n != c)
				throw new TokenizerException("Expecting " + c + " but found " + n, lineNo());
		}
		else
			throw new TokenizerException("Did not find " + c, lineNo());
	}

	/* Eat a word, or throw an exception */
	public void match(String s) throws TokenizerException {
		String n;
		if (peekAtKind() == TokenType.WORD) {
			n = getWord();
			if (!n.equals(s))
				throw new TokenizerException("Expecting " + s + " but found " + n, lineNo());
		}
		else
			throw new TokenizerException("Did not find " + s, lineNo());
	}

	/* Eat an operator, or return false */
	public boolean check(char c) {
		if (peekAtKind() == TokenType.OPERATOR) {
			if (c == getOp())
				return true;
			else {
				pushBack();
				return false;
			}
		}
		return false;
	}

	/* Eat a word, or return false */
	public boolean check(String s) {
		if (peekAtKind() == TokenType.WORD) {
			if (s.equals(getWord()))
				return true;
			else {
				pushBack();
				return false;
			}
		}
		return false;
	}

	/* Test an operator, or return false */
	public boolean test(char c) {
		boolean check = check(c);
		if (check)
			pushBack();
		return check;
	}

	/* Test a word, or return false */
	public boolean test(String s) {
		boolean check = check(s);
		if (check)
			pushBack();
		return check;
	}

	/* Put last read token back on the input stack */
	public void pushBack() {
		if (!readTokens.empty())
			tokens.push(readTokens.pop());
	}

	/* Is that possible? */
	public boolean canPushBack() {
		return !readTokens.empty();
	}
	
	/* Return the whitespace of the next token */
	public String getWhitespaceBeforeToken(){
		return tokens.peek().getWhitespace();
	}

	/* Return the line number of the last read token */
	public int lineNo() {
		if (readTokens.empty())
			return 1;
		else
			return readTokens.peek().lineNo();
	}

	/* Return the line number of the next token */
	public int nextLineNo() {
		if (tokens.empty())
			return lineNo();
		else
			return tokens.peek().lineNo();
	}

	/* Close the stream */
	public void close() {
		try {
			tokens.empty();
			readTokens.empty();
			in.close();
		}
		catch (IOException e) { }
	}

	/* Skips the next token */
	public void skipToken() {
		if (!tokens.empty())
			readTokens.push(tokens.pop());
	}

	/* This is the parent class for all tokens */
	private static abstract class Token {

		/* The line number of the token */
		protected int lineNo;
		
		/* The whitespace before the token */
		protected String whitespace;
		
		/* Returns the line number */
		public int lineNo(){
			return lineNo;
		}
		
		/* Returns the whitespace */
		public String getWhitespace(){
			return whitespace;
		}
		
		/* Return the type of this token */
		public abstract TokenType getType();
	}

	/* The INTEGER token */
	private static class IntToken extends Token {
		int integer;

		public IntToken(int integer, String whitespace, int lineNo) {
			this.lineNo = lineNo;
			this.whitespace = whitespace;
			this.integer = integer;
		}

		public int getInt() {
			return integer;
		}

		public String toString() {
			return whitespace + integer;
		}

		public TokenType getType() {
			return TokenType.INTEGER;
		}
	}

	/* The FLOAT token */
	private static class FloatToken extends Token {
		float f;

		public FloatToken(float fl, String whitespace, int lineNo) {
			this.lineNo = lineNo;
			this.whitespace = whitespace;
			this.f = fl;
		}

		public float getFloat() {
			return f;
		}

		public String toString() {
			return whitespace + f;
		}

		public TokenType getType() {
			return TokenType.FLOAT;
		}
	}

	/* The WORD token */
	private static class WordToken extends Token {
		String word;

		public WordToken(String word, String whitespace, int lineNo) {
			this.lineNo = lineNo;
			this.whitespace = whitespace;
			this.word = word;
		}

		public String getWord() {
			return word;
		}

		public String toString() {
			return whitespace + word;
		}

		public TokenType getType() {
			return TokenType.WORD;
		}
	}

	/* The OPERATOR token */
	private static class OperatorToken extends Token {
		char op;

		public OperatorToken(char op, String whitespace, int lineNo) {
			this.op = op;
			this.whitespace = whitespace;
			this.lineNo = lineNo;
		}

		public char getOp() {
			return op;
		}

		public String toString() {
			return whitespace + op;
		}

		public TokenType getType() {
			return TokenType.OPERATOR;
		}
	}
	
	/* The COMMENT token */
	private static class CommentToken extends Token {
		String comment;
		
		public CommentToken(String comment, String whitespace, int lineNo){
			this.comment = comment;
			this.whitespace = whitespace;
			this.lineNo = lineNo;
		}
		
		public String getComment() {
			return comment;
		}
		
		public String toString() {
			return whitespace + "//" + comment;
		}
		
		public TokenType getType() {
			return TokenType.COMMENT;
		}
	}
	
	/* The STRING token */
	private static class StringToken extends Token {
		String str;
		
		public StringToken(String str, String whitespace, int lineNo){
			this.str = str;
			this.whitespace = whitespace;
			this.lineNo = lineNo;
		}
		
		public String getString() {
			return str;
		}
		
		public String toString() {
			return whitespace + '"' + str + '"';
		}
		
		public TokenType getType() {
			return TokenType.STRING;
		}
	}

	/* The CHARACTER token */
	private static class CharToken extends Token {
		char c;
		
		public CharToken(char c, String whitespace, int lineNo){
			this.c = c;
			this.whitespace = whitespace;
			this.lineNo = lineNo;
		}
		
		public char getChar() {
			return c;
		}
		
		public String toString() {
			return whitespace + '\'' + c + '\'';
		}
		
		public TokenType getType() {
			return TokenType.CHARACTER;
		}
	}

	/* The EOF token */
	private static class EOFToken extends Token {
		public EOFToken(String whitespace, int lineNo){
			this.whitespace = whitespace;
			this.lineNo = lineNo;
		}
		
		public String toString() {
			return whitespace;
		}
		
		public TokenType getType() {
			return TokenType.EOF;
		}
	}
}
