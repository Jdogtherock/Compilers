package edu.cornell.cs.sam.core;
import edu.cornell.cs.sam.utils.RegistrationSystem;
import java.util.Collection;

/**
 * The Sys class provides a centralized point of access to all the system components.
 * It should be instantiated by the first class to use the new system, and sets up both
 * the CPU and the memory. It should be passed to any other components that need
 * access to the Processor, Memory, or Video components of the system. Also, please
 * remember to the the video component by calling setVideo().
 */
public class Sys {
	/**
	 * The version of the SaM Core
	 */
	public static final String SAM_VERSION = "2.6.2";

	private int procID, memID, vidID;

	/**
	 * Returns the first CPU (or null if no CPUs)
	 */
	public Processor cpu() {
		return (Processor) RegistrationSystem.getElement(procID);
	}
	/**
	 * Returns the CPU collection 
	 * (or null if no CPUs are available)
	 */
	public Collection<Processor> cpus() {
		return (Collection<Processor>) 
			RegistrationSystem.getElements(procID);
	}
	/**
	 * Returns the shared memory (or null if no memory)
	 */
	public Memory mem() {
		return (Memory) RegistrationSystem.getElement(memID);
	}
	/**
	 * Returns the video card (or null if no video card)
	 */
	public Video video() {
		return (Video) RegistrationSystem.getElement(vidID);
	}

	/**
	 * Sets the video card to the provided component
	 */
	public void setVideo(Video v) {
		RegistrationSystem.register(vidID, v);
	}

	/**
	 * Creates a new single cpu Sys.
	 */
	public Sys() {
		this(1);
	}

	/**
	 * Creates a new Sys with processor(s) and memory
	 * @param n - The number of processors to launch
	 */
	public Sys(int n) {
		procID = RegistrationSystem.getNextUID();
		memID = RegistrationSystem.getNextUID();
		vidID = RegistrationSystem.getNextUID();

		for (int i=0; i < n; i++) 
			RegistrationSystem.register(procID, new SamProcessor(this));

		Memory mem = new SamMemory(this);
		mem.setHeapAllocator(new ExplicitFreeAllocator());
		mem.getHeapAllocator().setMemory(mem);
		RegistrationSystem.register(memID, mem);
	}
}
