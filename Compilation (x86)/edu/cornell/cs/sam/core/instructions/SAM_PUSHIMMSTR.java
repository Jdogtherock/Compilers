package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.HeapAllocator;
import static edu.cornell.cs.sam.core.Memory.Type;

/**
 * Pushes the operand string on the heap, and store its address on the stack
 */

public class SAM_PUSHIMMSTR extends SamStringInstruction {
	public void exec() throws SystemException {
		final int size = op.length();

		HeapAllocator heap = mem.getHeapAllocator();
		if (heap == null) 
			mem.pushMA(0);
		else {
			heap.malloc(size+1); 			
			int addr = mem.getValue(cpu.get(SP) - 1); 
			int a;
			for (a = 0; a < size; a++)	
				mem.setMem(addr + a, (int) op.charAt(a), Type.CH);
			mem.setMem(addr + a, (int) '\0', Type.CH);
		}
		cpu.inc(PC);
	};
}


