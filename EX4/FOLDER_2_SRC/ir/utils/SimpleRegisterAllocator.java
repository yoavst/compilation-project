package ir.utils;

import ir.registers.Register;
import ir.registers.TempRegister;

/**
 * Simple register allocator using a counter
 */
public class SimpleRegisterAllocator implements RegisterAllocator {
    private int counter = 0;

    @Override
    public Register newRegister() {
        return new TempRegister(counter++);
    }

    @Override
    public void free(Register register) {
        if (!register.isConstant() && register.getId() == counter - 1) {
            counter--;
        }
    }

    @Override
    public void freeAll() {
        counter = 0;
    }
}
