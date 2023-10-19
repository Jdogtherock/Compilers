package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;
import edu.cornell.cs.sam.core.HeapAllocator;
import static edu.cornell.cs.sam.core.Memory.Type;

/**
 * Reads a string from the video interface,
 * stores that on the heap, and places its
 * address on the stack
 */

public class SAM_READSTR extends SamInstruction {
	public void exec() throws SystemException {
		String str = (video != null) ? video.readString() : "";
		final int size = str.length();

		HeapAllocator heap = mem.getHeapAllocator();
		if (heap == null)  
			mem.pushMA(0);
		else {
			heap.malloc(size + 1);
			int addr = mem.getValue(cpu.get(SP) - 1);
			int a;
			for (a = 0; a < size; a++)
				mem.setMem(addr + a, (int) str.charAt(a), Type.CH);
			mem.setMem(addr + a, (int) '\0', Type.CH);
		}
		cpu.inc(PC);
	};
}


