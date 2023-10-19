package edu.cornell.cs.sam.core;
import edu.cornell.cs.sam.core.instructions.Instruction;

/**
 * Implementation of a SaM Processor 
 */
public class SamProcessor implements Processor {

	public final static int REGISTERS = 4;
	private int[] registers = new int[] { 0, 0, 0, 0 };
        private static final String BR = System.getProperty("line.separator");

	private Program prg;
	private Sys sys;

	public SamProcessor(Sys sys) {
		this.sys = sys;
	}

        public Sys getSystem() {
		return sys;
	}

	/* Loads Program and registers instructions with the System */
	public void load(Program prog) throws SystemException {
		if (!prog.isExecutable()) 
			throw new SystemException("Program contains unresolved references: " + 
				BR + prog.getReferenceTable().toString());
		prg = prog;
	}

	public Program getProgram() {
		return prg;
	}

	/* Execution */
	public void step() throws SystemException {
		synchronized (prg) {
			Instruction i = prg.getInst(registers[PC]);
			i.setSystem(sys);
			i.exec();
		}
	}

	public void run() throws SystemException {
		while (registers[HALT] == 0) step();
	}

	/* Registers */
	public void init() {
		for (int i = 0; i < REGISTERS; i++)
			registers[i] = 0;
	}

	public int get(int reg) {
		return registers[reg];
	}

	public int[] getRegisters() {
		int[] regs = new int[REGISTERS];
		System.arraycopy(registers, 0, regs, 0, REGISTERS);
		return regs;
	}

	public void set(int reg, int value) throws SystemException {
		verify(reg, value);
		registers[reg] = value;
	}

	public int inc(int reg) throws SystemException {
		verify(reg, registers[reg] + 1);
		return ++registers[reg];
	}

	public int dec(int reg) throws SystemException {
		verify(reg, registers[reg] - 1);
		return --registers[reg];
	}

	public void verify(int reg, int value) throws SystemException {
		switch (reg) {
			case PC :
				if (value < 0 || value > prg.getLength() - 1)
					throw new SystemException("Invalid instruction index, PC = " + value);
				break;
			case SP :
				if (value < 0)
					throw new SystemException("Stack Underflow, SP = " + value);
				if (value > Memory.STACKLIMIT - 1)
					throw new SystemException("Stack Overflow, SP = " + value);
				break;
			case FBR :
				if (value < 0 || value > Memory.STACKLIMIT - 1)
					throw new SystemException("Invalid frame address, FBR = " + value);
				break;
			default :
		}
	}

}
