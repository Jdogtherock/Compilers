package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.HeapAllocator;

/**
 * Deallocates the given address, which was allocated
 * by SAM_MALLOC 
 */

public class SAM_FREE extends SamInstruction {
	public void exec() throws SystemException {
		
		HeapAllocator heap = mem.getHeapAllocator();
		
		int addr = mem.popMA();

		if (heap != null) 
			heap.free(addr);
		cpu.inc(PC);
	}
}
