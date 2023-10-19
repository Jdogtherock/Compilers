package edu.cornell.cs.sam.utils;

/**
 * The base class for threads in SaM GUIs. It implements the basics
 * that all such threads responsible for running code need
 */
public abstract class SamThread extends Thread {

	/* true when execution stop has been requested */
	private volatile boolean stopRequested = false;

	/* The parent of this thread */
	private ThreadParent parent = null;

	/**
	 * A thread status indicating that the thread has been interrupted
	 */
	public static final int THREAD_INTERRUPTED = 0;
	
	/**
	 * A thread status indication there was an exception in the thread
	 */
	public static final int THREAD_EXCEPTION = 1;

	/**
	 * A thread status indication successful completion
	 */
	public static final int THREAD_EXIT_OK = 2;

	/**
	 * Sets the parent to the provided parent
	 * param parent The parent to set 
	 */
	public void setParent(ThreadParent parent) {
		this.parent = parent;
	}

	/**
	 * Returns the thread parent
	 * @return the parent of this thread
	 */
	public ThreadParent getParent() {
		return this.parent;
	}

	/**
	 * Interrupts the thread
	 */
	public void interrupt() {
		stopRequested = true;
		super.interrupt();
	}

	/**
	 * Check if an interrupt has been requested
	 * @return true if an interrupt was requested, false otherwise
	 */
	public boolean interruptRequested() {
		return stopRequested;
	}

	/**
	 * Initiates thread execution.
	 */
	public void run() {
		try {
			execute();
		}

		catch (Exception e) {
			parent.threadEvent(THREAD_EXCEPTION, e);
			return;
		}
	}

	/**
	 * The main method used to execute one step. This method must
	 * check the stopRequested flag and respect its status.
	 */
	public abstract void execute() throws Exception;

	/**
	 * The parent of a thread 
	 */
	public interface ThreadParent {
		/**
		 * Called when a thread event has occurred
		 * @param ecode The integer code representing the event
		 * @param edata An optional Object to store event data
		 */
		public abstract void threadEvent(int ecode, Object edata);
	}

}
