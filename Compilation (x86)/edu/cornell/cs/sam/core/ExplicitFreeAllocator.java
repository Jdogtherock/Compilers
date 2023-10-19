package edu.cornell.cs.sam.core;
import static edu.cornell.cs.sam.core.Memory.Type;
import java.util.Iterator;

/**
 * This allocator provides support
 * for explicit malloc()/free()
 * Expects heap size larger than Memory.UNIT_SIZE 
 */

public class ExplicitFreeAllocator implements HeapAllocator {

	/* Debug */
	private final static boolean DEBUG_ALLOCATOR = false;

	/* Bitwise masks */
	private final static int STATUS_MASK = 1 << (Memory.UNIT_SIZE - 1);
	private final static int SIZE_MASK = (1 << (Memory.UNIT_SIZE - 1)) - 1;

	/* Heap constants */
	private final static int HEAP_BASE = Memory.STACKLIMIT;
	private final static int HEAP_TOP = Memory.MEMORYLIMIT;
	private final static int HEAP_SIZE = HEAP_TOP - HEAP_BASE;

	/* Anchor constants */
	private final static int ANCHOR_BASE = Memory.STACKLIMIT;
	private final static int ANCHOR_TOP = Memory.STACKLIMIT + Memory.UNIT_SIZE;
	private final static int ANCHOR_SIZE = Memory.UNIT_SIZE;	

	/* Anchor 0 can be used to track allocations, since it's below
	 * the MIN_SLICE limit and isn't used for anything else */
	private final static int ALLOC_ANCHOR = 0;

	/* Memory block offsets */
	private final static int METADATA_SIZE = 5;
	private final static int SLICE_SIZE_OFFSET = 0;
	private final static int BIN_PREV_OFFSET = 1;
	private final static int BIN_NEXT_OFFSET = 2;
	private final static int REQ_SIZE_OFFSET = 3;
	private final static int DATA_OFFSET = 4;
	private final static int SLICE_SIZE_END_OFFSET = 1;

	/* Minimum required for metadata - we need two fields for
	 * bin list traversal, and two fields for size/status */
	private final static int MIN_SLICE_SIZE = METADATA_SIZE;


	private Memory mem = null;

	/* Set/get */
	public Memory getMemory() {
		return mem;
	}

	public void setMemory(Memory mem) {
		this.mem = mem;
	}

	/* Debug helper - prints the bin free chains */
	private void printBins(String caller) {
		String str;

		if (DEBUG_ALLOCATOR) { 

		System.out.println();
		System.out.println("DEBUG: Free memory slices after " + caller);
		System.out.println("DEBUG:_______________________________________________________");	

		try {
			/* Loop over the anchors */
			for (int i=ANCHOR_BASE; i < ANCHOR_TOP; i++) {
				str= "anchor[" + (i-ANCHOR_BASE) + ":" + i + "]";
				int ptr = mem.getValue(i);

				/* Skip free anchors */
				if (ptr == 0) continue;

				else do {
					str += "<->" + ptr + "[" + getSize(ptr) + "]"; 

					int save = ptr;
					ptr = mem.getValue(ptr + BIN_NEXT_OFFSET);	

					/* Detect tear in the doubly linked list */
					if (ptr != 0 && mem.getValue(ptr + BIN_PREV_OFFSET) != save)
						System.out.println("DEBUG: Warning: list mismatch (orig= " + 
							save + ", succ = " + ptr + ", pred= " + 
							mem.getValue(ptr + BIN_PREV_OFFSET) + ")"); 
				} while (ptr != 0);
			
				if (i == ANCHOR_BASE)
					str += "      (ALLOCATIONS)";
				
				System.out.println("DEBUG: " + str);
			}
			System.out.println();
		}
		/* Should not happen */
		catch (SystemException e) { }

		}
	}

	/* Initialize */
	public void init() {

		try { 

			if (mem == null) return;
	
			/* Zero the anchor */	
			for (int i=ANCHOR_BASE; i < ANCHOR_TOP; i++)
				mem.setMem(i, 0, Type.MA);

			/* Distribute the rest of memory among bins */
			int ptr = HEAP_BASE + ANCHOR_SIZE;
			int size = HEAP_SIZE - ANCHOR_SIZE;
			distribute(ptr, size);
	
		/* Should not happen */
		} catch (SystemException e) { } 

		/* Debug */
		printBins("init()");
	}

	/* Return the proper bin for the requested size. 
	 * This returns a single bin sufficiently large for size */
	private static int getBin(int size) {
		int bin_idx;
		int top_bit = (1 << (ANCHOR_SIZE - 1));

		if (size < MIN_SLICE_SIZE)
			size = MIN_SLICE_SIZE;

		bin_idx = ANCHOR_SIZE - 1;
		while (bin_idx > 0 && (size & top_bit) == 0) {
			size <<= 1;
			bin_idx--;
		}

		/* For exact power of two, return correct size block */
		if ((size & (top_bit - 1)) == 0) 
			return bin_idx;
		
		/* Otherwise return something larger that will fit */
		else return bin_idx + 1;
	}

	/* Distribute a chunk of memory among free bins */
	private void distribute(int ptr, int size) throws SystemException {
		int top_bit = (1 << (ANCHOR_SIZE - 1));
		int bin_idx = ANCHOR_SIZE - 1;

		while (bin_idx > 0) {
			if ((size & top_bit) != 0) {
				int bin_size = 1 << bin_idx;
				attachToAnchor(bin_idx, ptr);
				setSizeStatus(ptr, bin_size, false);
				ptr += bin_size;
			}

			size <<= 1;
			bin_idx--;
		}
	}


	/* Detach a slice from the anchor (malloc), and return its address */
	private int detachFromAnchor(int bin_idx) throws SystemException {
		int allocated = mem.getValue(bin_idx + ANCHOR_BASE);
		int successor = mem.getValue(allocated + BIN_NEXT_OFFSET);	
		if (successor != 0) {
			/* Important - the predecessor of an anchor block
			 * is marked negative to distinguish it, but it
			 * also contains the index of the anchor.
			 * Usually the size determines the anchor, but this is
			 * not true for the allocations anchor */

			mem.setMem(successor + BIN_PREV_OFFSET, -bin_idx, Type.MA);
		}
		mem.setMem(bin_idx + ANCHOR_BASE, successor, Type.MA);
		
		return allocated;	
	}

	/* Attach a slice to the anchor (free)) */
	private void attachToAnchor(int bin_idx, int addr) throws SystemException {
		int successor = mem.getValue(bin_idx + ANCHOR_BASE);
	
		/* Important - the predecessor of an anchor block
		 * is marked negative to distinguish it, but it
		 * also contains the index of the anchor.
		 * Usually the size determines the anchor, but this is
		 * not true for the allocations anchor */
		mem.setMem(addr + BIN_PREV_OFFSET, -bin_idx, Type.MA);

		mem.setMem(addr + BIN_NEXT_OFFSET, successor, Type.MA);
		if (successor != 0)
			mem.setMem(successor + BIN_PREV_OFFSET, addr, Type.MA);
		mem.setMem(bin_idx + ANCHOR_BASE, addr, Type.MA);
	} 

	/* Unlink a slice from its bin */
	private void unlinkFromBin(int addr) throws SystemException {
		int prev = mem.getValue(addr + BIN_PREV_OFFSET);
		int next = mem.getValue(addr + BIN_NEXT_OFFSET);
	
		/* If unlinking an anchor */
		if (prev <= 0) detachFromAnchor(-prev);

		/* Regular link in the chain */
		else {	
			mem.setMem(prev + BIN_NEXT_OFFSET, next, Type.MA);
			if (next != 0) mem.setMem(next + BIN_PREV_OFFSET, prev, Type.MA);
		}
	}

	/* Set the size/status of a memory slice */
	private void setSizeStatus(int addr, int size, boolean used) throws SystemException {
		int size_status = (used)? STATUS_MASK | size : size;
		mem.setMem(addr + SLICE_SIZE_OFFSET, size_status, Type.INT);
		mem.setMem(addr + size - SLICE_SIZE_END_OFFSET, size_status, Type.INT);
	}

	/* Get the size of a memory slice */
	private int getSize(int addr) throws SystemException {
		return SIZE_MASK & mem.getValue(addr + SLICE_SIZE_OFFSET);
	}

	/* Get the size of the predecessor */
	private int getPrevSize(int addr) throws SystemException {
		return SIZE_MASK & mem.getValue(addr - SLICE_SIZE_END_OFFSET);
	}

	/* Get the size of the successor */
	private int getNextSize(int addr) throws SystemException {
		return SIZE_MASK & mem.getValue(addr + getSize(addr) + SLICE_SIZE_OFFSET);
	}

	/* Get whether a memory slice is used or not */
	private boolean isUsed(int addr) throws SystemException {
		return ((STATUS_MASK & mem.getValue(addr + SLICE_SIZE_OFFSET)) == 0)? false: true;
	}

	/* Get whether the predecessor is used */
	private boolean isPrevUsed(int addr) throws SystemException {
		return ((STATUS_MASK & mem.getValue(addr - SLICE_SIZE_END_OFFSET)) == 0)? false: true;
	}

	/* Get whether the successor is used */
	private boolean isNextUsed(int addr) throws SystemException {
		return ((STATUS_MASK & mem.getValue(addr + getSize(addr) + SLICE_SIZE_OFFSET)) == 0)? false: true;
	}

	/* Allocate a slice of memory */
	public void malloc(int req_size) throws SystemException {

		if (mem == null) return;
		if (req_size < 0) return;

		/* Accounting data */
		int size = req_size + METADATA_SIZE;
	
		/* Find initial index - best fit */
		int best_fit_idx = getBin(size);
		int true_idx = best_fit_idx;

		/* If no slice of that size is available,
		 * try to find a larger slice */		
		while (true_idx < ANCHOR_SIZE && mem.getValue(ANCHOR_BASE + true_idx) == 0)
			true_idx++;

		/* Not found */
		if (true_idx == ANCHOR_SIZE)
			throw new SystemException("malloc(): Insufficient memory");
	
		/* Retrieve block to allocate, and link its successor in the anchor */
		int allocated = detachFromAnchor(true_idx);		

		/* Calculate portion that will actually be used, and create that slice */
		int used_size = 1 << best_fit_idx;
		int remaining_size = getSize(allocated) - used_size;
		setSizeStatus(allocated, used_size, true);	
		mem.setMem(allocated + REQ_SIZE_OFFSET, req_size, Type.INT);
		attachToAnchor(ALLOC_ANCHOR, allocated);

		/* Distribute any remainder that exceeds twice the minimal slice,
		 * or if that doesn't work, add it to the allocation */
		int remaining_addr = allocated + used_size;
		if (remaining_size >= 2 * MIN_SLICE_SIZE)
			distribute(remaining_addr, remaining_size);
		else
			setSizeStatus(allocated, used_size + remaining_size, true);

		/* Push address on the stack */
		mem.pushMA(allocated + DATA_OFFSET);

		/* Debug */
		printBins("malloc(" + req_size +  " -> " + size + ")");
	}

	/* Frees the given address */
	public void free(int req_addr) throws SystemException {

		int addr = req_addr - DATA_OFFSET;

		if (mem == null) return;

		if (req_addr < 0 || req_addr > HEAP_TOP) 
			throw new SystemException("free(): Attempted to free invalid address " + req_addr);
						
		else if (req_addr < HEAP_BASE) 
			throw new SystemException("free(): Attempted to free stack address " + req_addr);

		if (!isUsed(addr)) 
			throw new SystemException("free(): Address " + req_addr + " is already free");

		/* Initial free block info */
		int free_start = addr;
		int free_size = getSize(addr);
		int this_size = free_size;

		/* If the preceding blocks are free, coalesce.
		 * There is either one preceding block, or a chain
		 * which follows the invariant that every free block
		 * preceding another is in a larger bin. 
		 * This keeps the algorithm O(1) */

		int ptr = addr;
		while (ptr > ANCHOR_TOP && !isPrevUsed(ptr)) {
			int prev_size = getPrevSize(ptr);
			free_size += prev_size;
			free_start = free_start - prev_size;
			unlinkFromBin(ptr - prev_size);
			ptr = free_start;
		}

		/* If consequent blocks are free, coalesce.
		 * There is either one following block, or
		 * a chain which follows the invariant that every
		 * free block following another is in a smaller bin.
		 * This keeps the algorithm O(1) */

		ptr = addr;
		int ptr_size = this_size;
		while ((ptr + ptr_size < HEAP_TOP) && !isNextUsed(ptr)) {
			int next_size = getNextSize(ptr);
			free_size += next_size;
			unlinkFromBin(ptr + ptr_size);
			ptr = ptr + ptr_size;
			ptr_size = next_size;
		}
		
		/* Unlink the current block from allocations. */
		unlinkFromBin(addr);

		/* Re-distribute */
		distribute(free_start, free_size);			

		/* Debug */
		printBins("free(" + req_addr + " -> " + addr + ")");
	}
 
	/* Iterate over allocations. They are not sorted by address */
	public Iterator<Allocation> getAllocations() {
		return new Iterator<Allocation>() { 

			private int current_addr = ANCHOR_BASE + ALLOC_ANCHOR;
			private boolean first = true;

			/* Is there another allocated block? */
			public boolean hasNext() { 	
		
				if (mem == null) return false;
				
				try { 
					if (first)
						return (mem.getValue(current_addr) != 0);
					else
						return (mem.getValue(current_addr + BIN_NEXT_OFFSET) != 0);
				}
		
				/* Should not happen */
				catch (SystemException e) { return false; }
			}

			/* Iterate to next allocated block */
			public Allocation next() {
				try { 
					if (!hasNext()) return null;
	
					if (first) {
						first = false;
						current_addr = mem.getValue(current_addr);
					} else 
						current_addr = mem.getValue(current_addr + BIN_NEXT_OFFSET);


					return new Allocation(current_addr + DATA_OFFSET,
						mem.getValue(current_addr + REQ_SIZE_OFFSET));

				/* Should not happen */
				} catch (SystemException e) { return null; }
			}

			/* Unimplemented */
			public void remove() { }
		};
	}
}
