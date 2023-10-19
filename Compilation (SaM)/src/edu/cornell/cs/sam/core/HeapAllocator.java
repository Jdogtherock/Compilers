package edu.cornell.cs.sam.core;
import java.util.Iterator;

/**
 * The HeapAllocator manages the SaM heap
 */

public interface HeapAllocator {

	/**
	 *  Represents a heap allocation 
	 **/
	public static class Allocation {
		private int addr, size;
		
		/**
	 	 * Create a new allocation
		 * @param addr The start address of the allocation		
		 * @param size The size of the allocation
		 */
		public Allocation(int addr, int size) {
			this.addr = addr;
			this.size = size;
		}

		/**
		 * Gets the start address of this allocation
		 * @return The start address
	 	 */
		public int getAddr() {
			return addr;
		}
	
		/**
		 * Gets the size of this allocation
		 * @return The allocation size
		 */
	
		public int getSize() {
			return size;
		}
	}

	/**
	 * Resets any internal state in the heap allocator to initial state.
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract void init();

	/**
	 * Allocates the specified amount of memory on the heap,
	 * and pushes its address on the stack.
	 * @param size the amount of memory to allocate
	 * @throws SystemException if there is an error allocating memory
	 */
	public abstract void malloc(int size) throws SystemException;

	/**
	 * Frees the given position in memory, which must have
	 * been allocated with malloc() 
	 * @throws SystemException if there is an error freeing memory
	 */
	public abstract void free(int pos) throws SystemException; 

	/**
	 * Sets the memory object of this allocator
	 * @param mem the memory 
	 */
	public abstract void setMemory(Memory mem);

	/**
	 * Gets the memory object of this allocator
	 * @return The memory object
	 */
	public abstract Memory getMemory();

	/**
	 * Gets an iterator to the allocations on the heap
	 * @return an allocation iterator
	 */
	public abstract Iterator<Allocation> getAllocations();

}
