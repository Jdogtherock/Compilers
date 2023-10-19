package edu.cornell.cs.sam.core.instructions;
import edu.cornell.cs.sam.core.SystemException;

/**
 * Divides two floating point numbers.
 */

public class SAM_DIVF extends SamInstruction {
        public void exec() throws SystemException {
                float divisor = mem.popFLOAT();
                float divided = mem.popFLOAT();

                mem.pushFLOAT(divided / divisor);
                cpu.inc(PC);
        }
}
