package edu.cornell.cs.sam.utils;
import static edu.cornell.cs.sam.core.Memory.Data;
import java.util.List;

/**
 * Holds the processor state
 */
public class ProgramState implements java.io.Serializable {
	private List<? extends Data> stack;
	private int[] registers;
	private int lastpc;

	public ProgramState(int lastpc, List<? extends Data> stack, int[] regs) {
		this.lastpc = lastpc;
		this.stack = stack;
		registers = regs;
	}

	public int getLastPC() {
		return lastpc;
	}

	public int[] getRegisters() {
		return registers;
	}

	public List<? extends Data> getStack() {
		return stack;
	}
}
