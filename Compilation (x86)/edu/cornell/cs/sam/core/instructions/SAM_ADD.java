package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import static edu.cornell.cs.sam.core.Memory.Data;
import static edu.cornell.cs.sam.core.Memory.Type;

/**
 * Adds two locations on the stack, taking into account type promotion
 * to MA or PA. 
 */

public class SAM_ADD extends SamInstruction {
	public void exec() throws SystemException {
		Data two = mem.pop();
		Data one = mem.pop();
		Type t1 = one.getType();
		Type t2 = two.getType();
		Type t_new;	

		if ((t1 == Type.MA && t2 == Type.INT) || (t1 == Type.INT && t2 == Type.MA)) 
			t_new = Type.MA;
		else if ((t1 == Type.PA && t2 == Type.INT) || (t1 == Type.INT && t2 == Type.PA))
			t_new = Type.PA;
		else
			t_new = Type.INT;

		Data result = new Data(one.getValue() + two.getValue(), t_new);
		mem.push(result);
		cpu.inc(PC);
	}
}
