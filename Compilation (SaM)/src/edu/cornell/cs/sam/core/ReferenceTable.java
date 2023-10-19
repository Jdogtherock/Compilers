package edu.cornell.cs.sam.core;
import java.util.Collection;

/**
 * The Reference Table stores information about unresolved symbols - 
 * it keeps track of references to each symbol. 
 */
public interface ReferenceTable {
        /**
         * Adds a new reference to a symbol 
         */
        void add(String symbol, int ref_address);
	/** 
  	 * Deletes a symbol and all references to it 
	 */
	public void deleteSymbol(String symbol);
	/**
    	 * Returns the addresses to all references
	 * to a particular symbol. 
	 */
	public Collection<Integer> getReferences(String symbol);
        /**
         * Returns a string version of the table
         */
        String toString();
	/**
	 * Returns the size of the reference table 
	 */
	int size();
}
