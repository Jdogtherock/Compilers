package edu.cornell.cs.sam.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/*
 * Static registration system for Sam components. It can register and classify
 * components based on a classID. This class is thread safe.
 */

public class RegistrationSystem {
	private static Map<Integer,Map<?,?>> registrations = 
		new HashMap<Integer,Map<?,?>>();
	private static int uid = 0;

	/* 
	 * Register object with the given class id 
	 */
	public synchronized static <T> void register(int classID, T obj) {
		Map<T,T> d;
		d = (Map<T,T>) registrations.get(classID);
		if (d == null) {
			d = new HashMap<T,T>();
			registrations.put(classID, d);
		}

		d.put(obj, obj);
	}

	/*
	 * Unregister the object with the given class ID 
 	 * If this is the last object of this class unregisters the class as well.
	 */
	public synchronized static void unregister(int classID, Object obj) {
		Map<?,?> d = registrations.get(classID);
		if (d != null) {
			d.remove(obj);	
			if (d.size() == 0) 
				registrations.remove(classID);
		}
	}	 
	
	/*
	 * Unregister all elements with the given class ID 
	 */
	public synchronized static void unregister(int classID) {
		registrations.remove(classID);
	}

	/* Get all elements with the given classID. Can be null. */
	public synchronized static Collection<?> getElements(int classID) {
		Map<?,?> d = registrations.get(classID);
		if (d == null) return null;
		else return d.keySet();
	}
	
	/* Get the first element with the given classID. Can be null. */
	public synchronized static Object getElement(int classID) {
		Map<?,?> d = registrations.get(classID);
		if (d == null) return null;
		Set<?> ks = d.keySet();
		if (ks.isEmpty()) return null;
		else return ks.iterator().next();
	}
	
	/* Get the next unique ID */
	public synchronized static int getNextUID() {
		return uid++;
	}
}

