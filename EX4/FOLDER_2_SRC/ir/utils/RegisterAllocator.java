package ir.utils;

import ir.registers.Register;

public interface RegisterAllocator {
    /**
     * Allocate new temporary register
     */
    Register newRegister();

    /**
     * free specific register
     */
    void free(Register register);

    /**
     * Free all used registers
     */
    void freeAll();
}
