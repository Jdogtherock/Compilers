import java.io.*;
import java.util.Hashtable;
import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.TokenParseException;
import edu.cornell.cs.sam.io.Tokenizer;
import edu.cornell.cs.sam.io.TokenizerException;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;

// Receives Bali code and prints SAM code. Parses through the Bali code using SAMTokenizer
public class BaliCompiler
{
	static String compiler(String fileName)
	{

		try
		{
			SamTokenizer f = new SamTokenizer (fileName);
			String pgm = getProgram(f);
			System.out.println(pgm);
			return pgm;
		}
		catch (Exception e)
		{
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}

	// PROGRAM -> METH_DECL*
	static String getProgram(SamTokenizer f)
	{
		try
		{
			String pgm="PUSHIMM 0\nLINK\nJSR mainSTART\nPOPFBR\nSTOP\n";
			Hashtable<String, Integer> methods = new Hashtable<String, Integer>();
			int[] loops = new int[]{0, 0};
			while(f.peekAtKind()!=TokenType.EOF)
			{
				pgm+= getMethod(f, methods, loops);
			}
			return pgm;
		}
		catch(Exception e)
		{
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}

	// Method Declaration, not invocation
	static String getMethod(SamTokenizer f, Hashtable<String, Integer> methods, int[] loops)
	{
		f.match("int");
		String name = f.getWord();
		VarTable v = new VarTable();
		v.set_name(name);
		f.match('(');
		if(!name.equals("main")){
			getFormals(f, v); // num args in the table is updated to reflect the number of formals
		}
		f.match(')');
		if(!methods.containsKey(name)){
			methods.put(name, v.num_args());
		} else if (v.num_args() != methods.get(name)){
			throw new TokenizerException("Parameter count mismatch");
		}
		// Assume that RV and LINK are set up wherever the method was called
		// RV FILLED, LOCAL VARS POPPED.
		String b = body(f, v, methods, loops);
		return name + "START:" + b;
	}

	// Fills symbol table (for parameters)
	static void getFormals(SamTokenizer f, VarTable v){
		// First arg
		f.match("int");
		String formals = "";
		formals += f.getWord() + " ";
		v.add_arg();
		// Other args
		while (f.test(',')){
			f.match(',');
			f.match("int");
			formals += f.getWord();
			if(f.test(',')){
				formals += " ";
			}
			v.add_arg();
		}
		String[] args = formals.split(" ");
		// At this point, formals will be like: "a b e m"
		// num args is the total number of arguments
		for (int i = 0 - v.num_args();  i < 0; i++){
			v.set(args[i + v.num_args()], i);
		}
	}

	static String body(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods, int[] loops){
		f.match('{');
		String bodyCommands = "";
		while(f.test("int")){ // if variables are being declared, must start with type
			bodyCommands += decl_var(f, v, methods);
		}
		while(!f.test('}')){ // if we havent hit closing bracket yet, consider it a statement
			bodyCommands += stmt(f, v, methods, loops, false);
		}
		f.match('}');
		return bodyCommands;
	}

	static String decl_var(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods){
		f.match("int");
		String variables = "";
		String name = f.getWord();
		if(v.in(name)){ // variable is already defined, throw error
			throw new TokenizerException("variable already declared.");
		}
		v.set(name, v.setLocal());
		variables += "ADDSP 1\n";
		if(f.test('=')){ // first local has a value
			f.match('=');
			variables += getExp(f, v, methods); // get value
			variables += "STOREOFF " + v.get(name) + "\n"; // store value in first locals spot
		}
		while(f.test(',')){ // we are declaring more locals
			f.match(',');
			name = f.getWord();
			if(v.in(name)){ // variable is already defined, throw error
				throw new TokenizerException("variable already declared.");
			}
			v.set(name, v.setLocal());
			variables += "ADDSP 1\n";
			if(f.test('=')){ // variable has a value
				f.match('=');
				variables += getExp(f, v, methods); // get value
				variables += "STOREOFF " + v.get(name) + "\n"; // store value
			}
		} // if theres multiple other vars, a ',' should be next
		f.match(';'); // if a ',' isnt next, then it has to be a ';'
		return variables;
	}

	static String stmt(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods, int[] loops, boolean inLoop){
		if (f.test("return")){
			f.match("return");
			String retVal = getExp(f, v, methods);
			f.match(';');
			String storeRet = "STOREOFF " + (-1 * v.num_args() - 1) + "\n";
			String popLocals = "ADDSP " + (-1 * (v.size() - v.num_args())) + "\n";
			String jmp = "JUMPIND\n";
			return retVal + storeRet + popLocals + jmp;
		} else if (f.test("if")){
			// must add a JUMP elseEnd if u make it inside the ifBody, and a elseEnd label after the else body
			f.match("if");
			f.match('(');
			String conditional = getExp(f, v, methods);
			f.match(')');
			int elseNum = loops[0];
			loops[0] = loops[0] + 1;
			String elseLabel = "else" + elseNum + ":\n";
			String elseJump = "JUMPC else" + elseNum + "\n";
			String compare = "PUSHIMM 0\nEQUAL\n"; // ONLY JUMP IF CONDITIONAL IS 0
			String ifBody = stmt(f, v, methods, loops, inLoop);
			f.match("else");
			String elseBody = stmt(f, v, methods, loops, inLoop);
			return conditional + compare + elseJump + ifBody + "JUMP elseEnd" + elseNum + "\n" + elseLabel + elseBody + "elseEnd" + elseNum + ":\n";
		} else if (f.test("while")){
			int loopNum = loops[1];
			loops[1] = loops[1] + 2;
			String jumpCond = "JUMP loop" + loopNum + "\n";
			String condLabel = "loop" + loopNum + ":\n";
			String whileBodyLabel = "loop" + (loopNum + 1) + ":\n";
			f.match("while");
			f.match('(');
			String conditional = getExp(f, v, methods);
			f.match(')');
			String whileBody = stmt(f, v, methods, loops, true);
			String loopJump = "JUMPC loop" + (loopNum + 1) + "\n";
			String loopEnd = "loopend" + (loopNum) + ":\n";
			return jumpCond + whileBodyLabel + whileBody + condLabel + conditional + loopJump + loopEnd;
		} else if (f.test("break")){
			if(!inLoop){
				throw new TokenizerException("Illegal break.");
			}
			f.match("break");
			f.match(';');
			while(!f.test('}')){
				f.skipToken();
			}
			int loopEnd = loops[1] - 2;
			String breakJump = "JUMP loopend" + loopEnd + "\n";
			return breakJump;
		} else if (f.test('{')){
			f.match('{');
			String statements = "";
			while(!f.test('}')){
				statements += stmt(f, v, methods, loops, inLoop);
			}
			f.match('}');
			return statements;
		} else if (f.test("';")){
			f.match(';');
			return "";
		} else {
			switch(f.peekAtKind()){
				case WORD:
					String name = f.getWord();
					if (!v.in(name)){
						throw new TokenizerException("Variable not found");
					}
					f.match('=');
					String expression = getExp(f, v, methods);
					String storeEXP = "STOREOFF " + v.get(name) + "\n";
					f.match(';');
					return expression + storeEXP;
				default:
					throw new TokenizerException("Invalid Token for statement.");
			}
		}
	}

	// Post: Final Value pushed to TOS
	static String getExp(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods) {
        switch (f.peekAtKind()) {
			case WORD: // -> VARIABLE OR METHOD
				String id = f.getWord();
				if(f.test('(')){ // METHOD CASE
					f.match('(');
					String s = "PUSHIMM 0\n"; // RV FOR METHOD
					int num_actuals = 0;
					if(!f.test(')')){ // IF ACTUALS EXIST
						s += getExp(f, v, methods);
						num_actuals += 1;
						while(f.test(',')){ // GET THE REST OF THE ACTUALS
							f.match(',');
							s += getExp(f, v, methods);
							num_actuals += 1;
						}
					}
					f.match(')');
					if(!methods.containsKey(id)){ // ACTUALS = FORMALS CHECK
						methods.put(id, num_actuals);
					} else if (num_actuals != methods.get(id)){ // METHODS BEEN SEEN
						System.out.println("Actuals and Formals Mismatch");
						throw new TokenizerException(null);
					}
					s += "LINK\n"; // PUSH FBR
					s += "JSR " + id + "START\n"; // JUMP TO METHOD. METHOD CONTAINS JUMPIND & WILL RETURN HERE.
					s += "POPFBR\n"; // POP FBR
					s += "ADDSP -" + num_actuals + "\n"; // POP PARAMETERS. LOCALS POPPED IN THE METHOD.
					return s;
				} else{ // Else, VARIABLE. PUSH ITS VALUE TO TOS. SHOULD BE IN VARTABLE.
					if(v.in(id)){ // VARIABLE IS IN VARTABLE
						return "PUSHOFF " + v.get(id) + "\n";
					} else { // VARIABLE NOT IN VARTABLE
						throw new TokenizerException("Variable not instantiated in this method.");
					}
				}
			case INTEGER:
				// INT, TRUE (1), FALSE (0)
				return "PUSHIMM " + f.getInt() + "\n";
			case OPERATOR:
				if(f.test('(')){
					f.match('(');
					if(f.test('-')){
						f.match('-');
						String exp1 = getExp(f, v, methods);
						f.match(')');
						String negate = "PUSHIMM -1\nTIMES\n";
						return exp1 + negate;
					} else if(f.test('!')){
						f.match('!');
						String exp1 = getExp(f, v, methods);
						f.match(')');
						String not = "NOT\n";
						return exp1 + not;
					} else {
						String exp1 = getExp(f, v, methods);
						if (f.test(')')){
							f.match(')');
							return exp1;
						} else if (f.test('+')){
							f.match('+');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "ADD\n";
						} else if (f.test('-')){
							f.match('-');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "SUB\n";
						} else if (f.test('*')){
							f.match('*');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "TIMES\n";
						} else if (f.test('/')){
							f.match('/');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "DIV\n";
						} else if (f.test('&')){
							f.match('&');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "AND\n";
						} else if (f.test('|')){
							f.match('|');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "OR\n";
						} else if (f.test('<')){
							f.match('<');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "LESS\n";
						} else if (f.test('>')){
							f.match('>');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "GREATER\n";
						} else if (f.test('=')){
							f.match('=');
							String exp2 = getExp(f, v, methods);
							f.match(')');
							return exp1 + exp2 + "EQUAL\n";
						} else {
							throw new TokenizerException("Invalid symbol in expression.");
						}
					}
				}
        	default:
				throw new TokenizerException("Invalid TokenType for Expression.");
        }
    }
}
