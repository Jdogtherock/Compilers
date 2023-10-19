package edu.cornell.cs.sam.core;
import java.util.List;
import static edu.cornell.cs.sam.core.HeapAllocator.Allocation;

/**
 * The Memory interface provides low-level memory access methods 
 */
public interface Memory {

        /**
         * The upper limit of memory
         */
        public final static int MEMORYLIMIT = 10000;

        /**
         * The upper limit of the stack
         */
        public final static int STACKLIMIT = 1000;

	/**
	 * Memory unit size in bits 
	 */
	public final static int UNIT_SIZE = 32;

        /**
	 * Represents memory data 
	 */
        public static class Data {
                private int value;
		private Type type;

		/**
	 	 * Create a new memory data object
		 * @param value The value
		 * @param type The type
		 */
		public Data(int value, Type type) {
			this.value = value;
			this.type = type;
		}

		/**
		 * Returns the value of this data object as stored in memory 
	 	 * @return The value as raw memory data
		 */
		public int getValue() {
			return value;
		}

		/**
		 * Returns the type of this data object
		 * @return The object type
	  	 */
		public Type getType() {
			return type;
		}

		/**
	 	 * Returns a descriptive string of this data object
	 	 * @return The string representation of this data object
		 */
		public String toString() {
			String ret = "";
			switch(type) {
				case INT: 
				case PA:
				case MA:
					ret += value;
					break;
				case CH:
					ret += "\'" + ((char) value) + "\'";
					break;
				case FLOAT: 
					ret += Float.intBitsToFloat(value);
					break;
				default:
					ret += getValue();
			}
			return ret;
		}	
        }

	/** 
	 * Represents memory type
	 */
	public static enum Type {
		/**
		 * Memory Address
		 */
		MA, 
		
		/**
		 * Integer
		 */
		INT,

		/**
		 * Floating Point Number
		 */ 
		FLOAT, 
		
		/**
		 * Program Address
		 */
		PA, 

		/**
		 * Character
		 */
		CH;

		/**
		 * Returns the integer code of this type 
		 * @return The corresponding integer code 
		 */
		public int toInt() {
			return ordinal();
		}

		/** 
		 * Returns the Type corresponding to this integer code
		 * @return The type corresponding to the provided code
		 */
		public static Type fromInt(int code) {
			switch(code) {
				case 0: return MA;
				case 1: return INT;
				case 2: return FLOAT;
				case 3: return PA;	
				case 4: return CH;
				default: return INT;
			}
		}
	}

	/**
	 * Initializes memory to default state 
	 **/
	public abstract void init();

        /** 
         * Return the Sys object associated with this memory.
	 * @return The Sys object
         **/
        public abstract Sys getSystem();

	/**
	 * Set the Sys object associated with this memory.
	 * @param sys The Sys object
	 **/
	public abstract void setSystem(Sys sys);

	/**
	 * Set the HeapAllocator object associated with this memory.
	 * @param ha The allocator
	 */
	public abstract void setHeapAllocator(HeapAllocator ha);

	/**
	 * Get the Heap Allocator object associated with this memory.
	 * @return The heap allocator
	 */ 
	public abstract HeapAllocator getHeapAllocator();

	/**
	 * Stores the given data object in memory
	 * @param addr The address in memory
	 * @param data The data object to set
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract void setMem(int pos, Data data) throws SystemException;

	/**
	 * Stores the given value/type pair in memory
	 * @param addr The address in memory
	 * @param value The value to store
	 * @param type The type of this value
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract void setMem(int pos, int value, Type type) throws SystemException;
	
	/**
	 * Stores the given value in memory
	 * @param addr The address in memory
	 * @param value The value to store
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract void setValue(int pos, int value) throws SystemException;

	/**
	 * Sets this location to the given type
	 * @param addr The address in memory
	 * @param type The type to set
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract void setType(int pos, Type type) throws SystemException;		

        /**
         * Returns the data object at the requested memory position
         * @param pos The memory address
         * @return The data object stored at the specified position
	 * @throws SystemException if there is an error accessing memory
         */
        public abstract Data getMem(int pos) throws SystemException;

	/**
	 * Returns the value at the requested memory position
	 * @param pos The memory address
	 * @return The value stored at the specified position
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract int getValue(int pos) throws SystemException;

	/**
	 * Returns the type at the requested memory position
	 * @param pos The memory address
	 * @return The type stored at the specified position
	 * @throws SystemException if there is an error accessing memory
	 */
	public abstract Type getType(int pos) throws SystemException;

	/**
	 * Returns an array containing the data for the given allocation.
	 * @param alloc The heap allocation
	 * @return The list of data for this allocation.
	 */
	public abstract List<Data> getAllocation(Allocation alloc);

	/**
	 * Returns a list containing the current stack
	 * @return The list of data.
	 */
	public abstract List<Data> getStack();

	/**
	 * Pushes a data object on the stack
	 * @param data The data object to push on the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void push(Data data) throws SystemException;

	/**
	 * Pushes a value/type pair on the stack
	 * @param value The value to push on the stack
	 * @param type The type of this value
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void push(int value, Type type) throws SystemException;

	/**
	 * Pushes an integer on the stack
	 * @param i The integer to push
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void pushINT(int i) throws SystemException;

	/**
	 * Pushes a character on the stack
	 * @param ch The character to push
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void pushCH(char ch) throws SystemException;
	
	/**
	 * Pushes a memory address on the stack
	 * @param ma The address to push
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void pushMA(int ma) throws SystemException;

	/**
	 * Pushes a program address on the stack
	 * @param pa The program address to push
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void pushPA(int pa) throws SystemException;
	
	/**
	 * Pushes a floating pointer number on the stack
	 * @param fl The floating point number to push
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract void pushFLOAT(float fl) throws SystemException;

	/**
	 * Pops a data object off the stack
	 * @return The data object popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract Data pop() throws SystemException;

	/**
	 * Pops a value off the stack
	 * @return The value popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract int popValue() throws SystemException;

	/**
	 * Pops an integer off the stack
	 * @return The integer popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract int popINT() throws SystemException;	

	/**
	 * Pops a character off the stack
	 * @return The character popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract char popCH() throws SystemException;

	/**
	 * Pops a memory address off the stack
	 * @return The memory address popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract int popMA() throws SystemException;
	
	/**
	 * Pops a program address off the stack
	 * @return The program address popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract int popPA() throws SystemException;

	/**
	 * Pops a floating pointer number off the stack
	 * @return The floating pointer number popped off the stack
	 * @throws SystemException if there is an error accessing the stack
	 */
	public abstract float popFLOAT() throws SystemException;
}
