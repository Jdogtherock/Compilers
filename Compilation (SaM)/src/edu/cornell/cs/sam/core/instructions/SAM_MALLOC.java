package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.HeapAllocator;

/**
 * Allocates the requested memory space on the heap
 * and pushes the resulting address on the stack
 */

public class SAM_MALLOC extends SamInstruction {
	public void exec() throws SystemException {
		
		HeapAllocator heap = mem.getHeapAllocator();
		
		if (heap != null) 
			heap.malloc(mem.popINT());
		else
			mem.pushINT(0);
		cpu.inc(PC);
	}
}
