package edu.cornell.cs.sam.core;

import java.util.Collection;

/**
 * The SymbolTable allows symbol to address and address to symbol
 * lookups.
 */
public interface SymbolTable {
	/**
	 * Adds a new pair
	 */
	void add(String symbol, int address);
	/**
	 * Return one symbol for a given address
	 */
	String resolveSymbol(int address);
	/**
	 * Returns all symbols for a given address
	 */
	Collection<String> resolveSymbols(int address);
	/**
	 * Returns an address for the given symbol
	 */
	int resolveAddress(String label);
	/**
	 * Returns the symbols contained in this table.
	 */
        Collection<String> getSymbols();

	/**
	 * Returns a string version of the table
	 */
	String toString();
}
