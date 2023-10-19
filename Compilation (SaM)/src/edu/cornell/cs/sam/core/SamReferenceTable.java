package edu.cornell.cs.sam.core;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;

/**
 * Reference table implementation using a hashtable of
 * array lists with references. 
 */
public class SamReferenceTable implements ReferenceTable, Serializable {
        private static final String BR = System.getProperty("line.separator");
	private HashMap<String, ArrayList<Integer> > references = 
		new HashMap<String, ArrayList<Integer> >();

	public void add(String symbol, int ref_address) {
        	ArrayList<Integer> srefs = references.get(symbol);
                if (srefs == null)
                	references.put(symbol, srefs = new ArrayList<Integer>());
                srefs.add (ref_address);
	}

	public void deleteSymbol(String symbol) {
		references.remove(symbol);	
	}

	public Collection<Integer> getReferences(String symbol) {
		return references.get(symbol);
	}

	public int size() {
		return references.size();
	}
	
	public String toString() {
		String ret = new String();	

		Set<String> symbols = references.keySet();

		for (String symbol: symbols) {
			ret += "Symbol \"" + symbol + "\" at addresses: ";
			ArrayList<Integer> srefs = references.get(symbol);
			for (Integer i: references.get(symbol)) 
				ret += i + " ";
			ret += BR;
		}
		return ret;
        }
}
