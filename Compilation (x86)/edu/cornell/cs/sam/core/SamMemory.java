package edu.cornell.cs.sam.core;
import java.util.List;
import java.util.LinkedList;
import static edu.cornell.cs.sam.core.Processor.SP;
import static edu.cornell.cs.sam.core.HeapAllocator.Allocation;

/**
	Memory implementation: Integer Array

	- Alternating data/type fields, 32-bit wide
	- Even addresses contain type
	- Odd addresses contain data
*/

public class SamMemory implements Memory {

	/* Memory array */
	private final static int INTERNALLIMIT = 2 * MEMORYLIMIT;
	private int[] memory = new int[INTERNALLIMIT];
	
	/* Set/ Get */
	private Sys sys = null;
	private Processor cpu = null;
	private HeapAllocator heap = null;

	public SamMemory(Sys sys) {
		setSystem(sys);
	}

	public Sys getSystem() {
		return sys;
	}

	public void setSystem(Sys sys) {
		this.sys = sys;
		this.cpu = sys.cpu();
	}

	public HeapAllocator getHeapAllocator() {
		return heap;
	}

	public void setHeapAllocator(HeapAllocator heap) {
		this.heap = heap;
	}

	/* Throw exception on invalid address */
	private void checkAddress(int pos) throws SystemException {
		if (pos < 0 || pos > MEMORYLIMIT -1)
			throw new SystemException("Invalid memory address: " + pos);
	}

	/* Low-level memory functions */
	public void init() {
		for (int i = 0; i < INTERNALLIMIT; i++)
			memory[i] = 0;

		if (heap != null) 
			heap.init();
	}

	public Data getMem(int pos) throws SystemException{
		checkAddress(pos);
		return new Data(memory[2 * pos + 1], Type.fromInt(memory[2*pos]));
	}

	public int getValue(int pos) throws SystemException {
		checkAddress(pos);
		return memory[2 * pos + 1];
	}

	public Type getType(int pos) throws SystemException {
		checkAddress(pos);
		return Type.fromInt(memory[2 * pos]);
	}

	public void setMem(int pos, Data data) throws SystemException{
		checkAddress(pos);	
		memory[2 * pos] = data.getType().toInt();
		memory[2 * pos + 1] = data.getValue();
	}

	public void setMem(int pos, int data, Type type) throws SystemException {
		checkAddress(pos);
		memory[2 * pos] = type.toInt();
		memory[2 * pos + 1] = data;
	}

	public void setValue(int pos, int data) throws SystemException {
		checkAddress(pos);
		memory[2 * pos + 1] = data;
	}

	public void setType(int pos, Type type) throws SystemException {
		checkAddress(pos);
		memory[2 * pos] = type.toInt();
	}

	/* Retrieve allocation */
	public List<Data> getAllocation(Allocation alloc) {
		List<Data> list = new LinkedList<Data>();
		final int limit = alloc.getAddr() + alloc.getSize();
		for (int a = alloc.getAddr(); a < limit; a++)
			list.add(new Data(memory[a * 2 + 1], Type.fromInt(memory[a * 2])));
			
		return list;
	}
	
	/* Stack zone management */
	public List<Data> getStack() {
		List<Data> list = new LinkedList<Data>();
		final int limit = cpu.get(SP);
		for (int a = 0; a < limit; a++)
			list.add(new Data(memory[a * 2 + 1], Type.fromInt(memory[a * 2])));
		
		return list;
	}

	public Data pop() throws SystemException {
		return getMem(cpu.dec(SP));
	}

	public int popValue() throws SystemException {
		return getValue(cpu.dec(SP));
	}
	
	public void push(Data data) throws SystemException {
		cpu.verify(SP, cpu.get(SP) + 1);
		setMem(cpu.get(SP), data);
		cpu.inc(SP);
	}

	public void push(int value, Type type) throws SystemException {
		cpu.verify(SP, cpu.get(SP) + 1);	
		setMem(cpu.get(SP), value, type);
		cpu.inc(SP);
	}

        /* Convenience methods */
	public float popFLOAT() throws SystemException {
		return Float.intBitsToFloat(popValue());
	}

	public void pushFLOAT(float fl) throws SystemException {
		push(Float.floatToIntBits(fl), Type.FLOAT);
	}

	public int popINT() throws SystemException {
		return popValue();
	}
	
	public void pushINT(int i) throws SystemException {
		push(i, Type.INT);
	}

	public char popCH() throws SystemException {
		return (char) popValue();
	}

	public void pushCH(char ch) throws SystemException {
		push(ch, Type.CH);
	}

	public int popPA() throws SystemException {
		return popValue();
	}

	public void pushPA(int pa) throws SystemException {
		push(pa, Type.PA);
	}
	
	public int popMA() throws SystemException {
		return popValue();
	}

	public void pushMA(int ma) throws SystemException {
		push(ma, Type.MA);
	}
}
