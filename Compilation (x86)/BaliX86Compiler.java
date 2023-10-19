import java.io.*;
import java.util.Hashtable;
import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.TokenParseException;
import edu.cornell.cs.sam.io.Tokenizer;
import edu.cornell.cs.sam.io.TokenizerException;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;
import java.util.LinkedList;


public class BaliX86Compiler
{
	static String compiler(String fileName)
	{
		try
		{
			SamTokenizer f = new SamTokenizer (fileName);
			String pgm = getProgram(f);
			pgm = "%include \"io.inc\"\n\n" + pgm;
			// System.out.println(pgm);
			return pgm;
		}
		catch (Exception e)
		{
			System.out.println("Fatal error: could not compile program");
			return "STOP\n";
		}
	}

	static String getProgram(SamTokenizer f)
	{
		try
		{
			String pgm="section .text\n    global CMAIN\n";
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
		String base = "push ebp\nmov ebp, esp\n";
		String b = body(f, v, methods, loops);
		if(name.equals("main")){
			return "CMAIN:\n" + base + b;
		} else{
			return name + "START:\n" + base + b;
		}
	}

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
		for (int i = 0;  i < v.num_args(); i++){
			v.set(args[i], 4 * (i + 1) + 4);
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
		variables += "sub dword esp, 4\n";
		if(f.test('=')){ // first local has a value
			f.match('=');
			variables += getExp(f, v, methods); // value in eax
			variables += "mov [ebp + " + v.get(name) + "], eax\n"; // mov eax to var offset
		}
		while(f.test(',')){ // we are declaring more locals
			f.match(',');
			name = f.getWord();
			if(v.in(name)){ // variable is already defined, throw error
				throw new TokenizerException("variable already declared.");
			}
			v.set(name, v.setLocal());
			variables += "sub dword esp, 4\n";
			if(f.test('=')){ // variable has a value
				f.match('=');
				variables += getExp(f, v, methods); // get value (in eax)
				variables += "mov [ebp + " + v.get(name) + "], eax\n"; // mov eax to the variable offset
			}
		} // if theres multiple other vars, a ',' should be next
		f.match(';'); // if a ',' isnt next, then it has to be a ';'
		return variables;
	}

	static String stmt(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods, int[] loops, boolean inLoop){
		if (f.test("return")){
			f.match("return");
			String _return = getExp(f, v, methods); // RETURN VALUE IN EAX
			f.match(';');
			String popLocals = "mov esp, ebp\n"; // POP LOCALS
			String popEBP = "pop ebp\n"; // POP EBP
			if(v.name.equals("main")){
				popEBP = "PRINT_DEC 4, eax\nNEWLINE\n" + popEBP;
			}
			String ret = "ret\n"; // GOTO RETURN ADDR
			return _return + popLocals + popEBP + ret;
		} else if (f.test("if")){
			f.match("if");
			f.match('(');
			int elseNum = loops[0];
			loops[0] = loops[0] + 1;
			String conditional = getExp(f, v, methods) + "if" + elseNum + "\n";
			f.match(')');
			String _if = "if" + elseNum + ":\n" + stmt(f, v, methods, loops, inLoop) + "ifEnd" + elseNum + ":\n";
			f.match("else");
			String _else = "else" + elseNum + ":\n" + stmt(f, v, methods, loops, inLoop) + "jmp ifEnd" + elseNum + "\n";
			return conditional + _else + _if;
		} else if (f.test("while")){
			int loopNum = loops[1];
			loops[1] = loops[1] + 2;
			String jumpCond = "jmp loop" + loopNum + "\n";
			String condLabel = "loop" + loopNum + ":\n";
			String whileBodyLabel = "loop" + (loopNum + 1) + ":\n";
			f.match("while");
			f.match('(');
			String conditional = getExp(f, v, methods) + "loop" + (loopNum + 1) + "\n";
			f.match(')');
			String whileBody = stmt(f, v, methods, loops, true);
			String loopEnd = "loopend" + (loopNum) + ":\n";
			return jumpCond + whileBodyLabel + whileBody + condLabel + conditional + loopEnd;
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
			String breakJump = "jmp loopend" + loopEnd + "\n";
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
				case WORD: // Assigning a variable to a variable
					String name = f.getWord();
					if (!v.in(name)){
						throw new TokenizerException("Variable not found");
					}
					f.match('=');
					String expression = getExp(f, v, methods); // Result is in eax
					String storeEXP = "mov [ebp + " + v.get(name) + "], eax\n"; // move eax into ebp + varOffset
					f.match(';');
					return expression + storeEXP;
				default:
					throw new TokenizerException("Invalid Token for statement.");
			}
		}
	}

	// POST: FINAL VALUE IN EAX
	static String getExp(SamTokenizer f, VarTable v, Hashtable<String, Integer> methods) {
        switch (f.peekAtKind()) {
			case WORD: // -> VARIABLE OR METHOD
				String id = f.getWord();
				if(f.test('(')){ // METHOD CASE
					f.match('(');
					String s = "";
					// PUSH ACTUALS
					int num_actuals = 0;
					if(!f.test(')')){
						s += getExp(f, v, methods);
						s += "push eax\n"; // push actual onto stack
						num_actuals += 1;
						while(f.test(',')){
							f.match(',');
							s += getExp(f, v, methods);
							s += "push eax\n";
							num_actuals += 1;
						}
					}
					// INVERT ACTUALS
					int offset = 4;
					for(int i = 0; i < num_actuals - 1; i++){
						s += "push dword [esp + " + offset + "]\n";
						offset += 8;
					}
					f.match(')');
					// ACTUALS = FORMALS CHECK
					if(!methods.containsKey(id)){ // ACTUALS = FORMALS CHECK
						methods.put(id, num_actuals);
					} else if (num_actuals != methods.get(id)){ // METHODS BEEN SEEN
						System.out.println("Actuals and Formals Mismatch");
						throw new TokenizerException(null);
					}
					// CALL METHOD
					s += "call " + id + "START\n";
					// RETURNED @ RETURN ADDR, METHOD RETURN VAL IN EAX, POP PARAMETERS
					s += "add dword esp, " + (num_actuals * 4) + "\n";
					return s;
				} else{ // VARIABLE CASE
					if(v.in(id)){
						// VARIABLES VALUE IN EAX
						return "mov dword eax, [ebp + " + v.get(id) + "]\n";
					} else { // VARIABLE NOT IN VARTABLE
						throw new TokenizerException("Variable not instantiated in this method.");
					}
				}
			case INTEGER:
					// MOVE INT INTO EAX
					return "mov dword eax, " + f.getInt() + "\n";
			case OPERATOR:
				if(f.test('(')){
					f.match('(');
					if(f.test('-')){
						f.match('-');
						String exp1 = getExp(f, v, methods); // E1 IN EAX
						f.match(')');
						String negate = "neg eax\n"; // -EAX
						return exp1 + negate;
					} else if(f.test('!')){
						f.match('!');
						String exp1 = getExp(f, v, methods); // E1 IN EAX
						f.match(')');
						String not = "not eax\n"; // !EAX
						return exp1 + not;
					} else {
						String exp1 = getExp(f, v, methods); // E1 IN EAX
						if (f.test(')')){
							f.match(')');
							return exp1;
						} else if (f.test('+')){
							f.match('+');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "add ebx, eax\nmov eax, ebx\n"; // E1 + E2 IN EAX
							return exp1 + e + exp2 + pop + arith;
						} else if (f.test('-')){
							f.match('-');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "sub ebx, eax\nmov eax, ebx\n"; // E1 - E2 IN EAX
							return exp1 + e + exp2 + pop + arith;
						} else if (f.test('*')){
							f.match('*');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "imul ebx, eax\nmov eax, ebx\n"; // E1 * E2 IN EAX
							return exp1 + e + exp2 + pop + arith;
						} else if (f.test('/')){ // IDIV: EAX/EBX
							f.match('/');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String move = "mov ebx, eax\n"; // E2 IN EBX
							String pop = "pop eax\n"; // E1 IN EAX
							String arith = "mov edx, 0\nidiv ebx\n"; // E1 / E2, RESULT IN EAX
							return exp1 + e + exp2 + move + pop + arith;
						} else if (f.test('&')){
							f.match('&');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "and ebx, eax\nmov eax, ebx\n"; // E1 & E2 IN EAX
							return exp1 + e + exp2 + pop + arith;
						} else if (f.test('|')){
							f.match('|');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "or ebx, eax\nmov eax, ebx\n"; // E1 | E2 IN EAX
							return exp1 + e + exp2 + pop + arith;
						} else if (f.test('<')){
							f.match('<');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "cmp ebx, eax\n"; // CMP MACHINE REGISTER CHANGED
							return exp1 + e + exp2 + pop + arith + "jl ";
						} else if(f.test('>')){
							f.match('>');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "cmp ebx, eax\n"; // CMP MACHINE REGISTER CHANGED
							return exp1 + e + exp2 + pop + arith + "jg ";
						} else if(f.test('=')){
							f.match('=');
							String e = "push eax\n"; // E1 ON TOS
							String exp2 = getExp(f, v, methods); // E2 IN EAX
							f.match(')');
							String pop = "pop ebx\n"; // E1 IN EBX
							String arith = "cmp ebx, eax\n"; // CMP MACHINE REGISTER CHANGED
							return exp1 + e + exp2 + pop + arith + "je ";
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
