package asm;

import ir.analysis.IRBlock;
import ir.analysis.IRBlockGenerator;
import ir.analysis.liveness.LimitedRegisterAllocator;
import ir.commands.IRCommand;
import ir.commands.arithmetic.*;
import ir.commands.flow.IRGotoCommand;
import ir.commands.flow.IRIfNotZeroCommand;
import ir.commands.flow.IRIfZeroCommand;
import ir.commands.flow.IRLabel;
import ir.commands.functions.*;
import ir.commands.memory.IRLoadAddressFromLabelCommand;
import ir.commands.memory.IRLoadCommand;
import ir.commands.memory.IRStoreCommand;
import ir.registers.*;
import ir.utils.IRContext;
import symbols.Symbol;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Mips {
    private static final String INDENTATION = "\t";
    private static final String NEWLINE = "\r\n";
    private static final int SIZE = IRContext.PRIMITIVE_DATA_SIZE;
    private static final int REGISTERS_COUNT = 8;
    private static final int COLORING_OFFSET = 8;
    private static final int REGISTERS_BACKUP_SIZE = REGISTERS_COUNT * SIZE;
    private static final int SKIP_SIZE = 2 * SIZE;
    private static final int $0 = 0;
    private static final int $v0 = 2;
    private static final int $a0 = 4;
    private static final int $t8 = 24;
    private static final int $t9 = 25;
    private static final int $sp = 29;
    private static final int $fp = 30;
    private static final int $ra = 31;
    private static final Map<Integer, String> registerNames = new HashMap<>();

    private StringBuilder dataSection = new StringBuilder();
    private StringBuilder codeSection = new StringBuilder();
    private Map<Register, IRLabel> globals = new HashMap<>();

    private Map<Register, Integer> localRegisters;
    private int parametersCount;
    private int localsCount;


    static {
        registerNames.put($0, "$0");
        registerNames.put($a0, "$a0");
        registerNames.put($v0, "$v0");
        registerNames.put($t8, "$t8");
        registerNames.put($t9, "$t9");
        registerNames.put($sp, "$sp");
        registerNames.put($fp, "$fp");
        registerNames.put($ra, "$ra");
        for (int i = 0; i < REGISTERS_COUNT; i++) {
            registerNames.put(COLORING_OFFSET + i, "$t" + i);
        }
    }

    public void process(List<IRCommand> commands, Map<IRLabel, List<IRLabel>> virtualTables, Map<String, IRLabel> constantStrings, Map<Symbol, Register> globals) {
        loadConstants(constantStrings);
        dataSection.append(NEWLINE);
        loadVirtualTables(virtualTables);
        dataSection.append(NEWLINE);
        loadGlobals(globals);
        jump(new IRLabel("main"));
        loadStdlib();
        loadCode(commands);
        try (PrintWriter out = new PrintWriter("mips.s")) {
            out.println(".data");
            out.println(dataSection);
            out.println();
            out.println();
            out.println(".text");
            out.println(codeSection);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //region Loading
    private void loadConstants(Map<String, IRLabel> constantStrings) {
        constantStrings.forEach((constant, label) -> dataSection.append(label).append(": .asciiz \"").append(constant).append("\"").append(NEWLINE));
    }

    private void loadVirtualTables(Map<IRLabel, List<IRLabel>> virtualTables) {
        virtualTables.forEach((label, methods) ->
                dataSection.append(label)
                        .append(": .word ")
                        .append(methods.stream().map(IRLabel::toString).collect(Collectors.joining(",")))
                        .append(NEWLINE)
        );
    }

    private void loadGlobals(Map<Symbol, Register> globals) {
        globals.forEach((symbol, register) -> {
            String varName = "_global_" + symbol.getName();
            dataSection.append(varName).append(": .word ").append(IRContext.NIL_VALUE).append(NEWLINE);
            this.globals.put(register, new IRLabel(varName));
        });
    }

    private void loadCode(List<IRCommand> commands) {
        // get blocks
        IRBlockGenerator generator = new IRBlockGenerator();
        for (IRCommand command : commands) {
            generator.handle(command);
        }
        List<IRBlock> blocks = generator.finish();
        // get program functions
        List<IRBlock> functions = blocks.stream().filter(IRBlock::isStartingBlock).collect(Collectors.toList());
        // handle each function
        functions.forEach(this::loadFunction);
    }

    private void loadFunction(IRBlock functionStartingBlock) {
        IRFunctionInfo functionInfo = (IRFunctionInfo) functionStartingBlock.commands.get(0);
        // get register coloring
        Set<IRBlock> wholeFunction = functionStartingBlock.scanGraph();
        localRegisters = new LimitedRegisterAllocator(REGISTERS_COUNT).allocateRealRegister(wholeFunction);
        parametersCount = functionInfo.numberOfParameters;
        localsCount = functionInfo.numberOfLocals;
        // update offset of coloring
        localRegisters.entrySet().forEach(e -> e.setValue(e.getValue() + COLORING_OFFSET));
        // generate function header
        //TODO set function id
        generateFunctionHeader(functionStartingBlock.label, functionInfo.numberOfParameters, functionInfo.numberOfLocals, 1);
        // generate function body
        IRBlock currentBlock = functionStartingBlock;
        do {
            if (IRContext.isReturnLabel(currentBlock.label)) {
                // generate function footer
                generateFunctionFooter(currentBlock.label, functionInfo.numberOfParameters, functionInfo.numberOfLocals);
                break;
            } else {
                // write label
                if (currentBlock != functionStartingBlock && currentBlock.label != null) {
                    label(currentBlock.label);
                }
                // write commands one after another
                currentBlock.commands.forEach(this::writeCommand);
            }
            currentBlock = currentBlock.realNextBlock;
        } while (currentBlock != null && !currentBlock.isStartingBlock());
    }

    /**
     * Function header format (each row  is 4 bytes:
     * header size in bytes
     * function id
     * return value
     * local n
     * local n-1
     * ...
     * local 1
     * return address
     * old frame pointer
     * <backup registers> - 8 registers
     * param n
     * param n-1
     * ...
     * param 1
     * ---------------------------------
     * params are already pushed at this point
     */
    private void generateFunctionHeader(IRLabel label, int parameters, int locals, int functionId) {
        // write label
        label(label);
        comment("[START] function header");
        // backup registers
        for (int i = 0; i < REGISTERS_COUNT; i++) {
            push(COLORING_OFFSET + i);
        }
        push($fp);
        push($ra);

        // update FP
        move($fp, $sp);
        // set default value to all locals and return value
        for (int i = 0; i <= locals; i++) {
            pushConst(0);
        }
        // push function id and header size
        pushConst(functionId);
        pushConst((parameters + 2 + locals + 3) * SIZE + REGISTERS_BACKUP_SIZE);
        comment("[END] function header");

    }

    private void generateFunctionFooter(IRLabel label, int parameters, int locals) {
        label(label);
        comment("[START] function footer");
        // ignore header size - only for stacktrace
        pop($v0);
        // ignore function id - only for stacktrace
        pop($v0);
        // save return value
        pop($v0);
        // go back to return address and pop it
        selfAddConst($sp, (locals * SIZE));
        pop($ra);
        // pop old frame pointer
        pop($fp);
        // restore registers
        for (int i = REGISTERS_COUNT - 1; i >= 0; i--) {
            pop(COLORING_OFFSET + i);
        }
        // skip parameters
        selfAddConst($sp, (parameters * SIZE));
        // return from function
        jumpRegister($ra);
        comment("[END] function footer");
    }

    private void writeCommand(IRCommand command) {
        if (command instanceof IRBinOpCommand) {
            binOpCommand(((IRBinOpCommand) command));
        } else if (command instanceof IRBinOpRightConstCommand) {
            binOpRightConstCommand(((IRBinOpRightConstCommand) command));
        } else if (command instanceof IRConstCommand) {
            constCommand(((IRConstCommand) command));
        } else if (command instanceof IRSetValueCommand) {
            setValueCommand(((IRSetValueCommand) command));
        } else if (command instanceof IRGotoCommand) {
            gotoCommand(((IRGotoCommand) command));
        } else if (command instanceof IRIfNotZeroCommand) {
            ifnzCommand(((IRIfNotZeroCommand) command));
        } else if (command instanceof IRIfZeroCommand) {
            ifzCommand(((IRIfZeroCommand) command));
        } else if (command instanceof IRCallCommand) {
            callCommand(((IRCallCommand) command));
        } else if (command instanceof IRCallRegisterCommand) {
            callRegisterCommand(((IRCallRegisterCommand) command));
        } else if (command instanceof IRPopCommand) {
            popCommand(((IRPopCommand) command));
        } else if (command instanceof IRPushCommand) {
            pushCommand(((IRPushCommand) command));
        } else if (command instanceof IRLoadCommand) {
            loadCommand(((IRLoadCommand) command));
        } else if (command instanceof IRLoadAddressFromLabelCommand) {
            loadAddressFromLabelCommand(((IRLoadAddressFromLabelCommand) command));
        } else if (command instanceof IRStoreCommand) {
            storeCommand(((IRStoreCommand) command));
        }
    }

    private void loadStdlib() {
        // division by zero
        dataSection.append("DIVISION_BY_ZERO: .asciiz \"Division By Zero\"").append(NEWLINE);
        label(IRContext.STDLIB_FUNCTION_THROW_DIVISION_BY_ZERO);
        loadAddress($a0, new IRLabel("DIVISION_BY_ZERO"));
        constant($v0, 4);
        syscall();
        constant($v0, 10);
        syscall();
        // null
        dataSection.append("NULL_POINTER: .asciiz \"Invalid Pointer Dereference\"").append(NEWLINE);
        label(IRContext.STDLIB_FUNCTION_THROW_NULL);
        loadAddress($a0, new IRLabel("NULL_POINTER"));
        constant($v0, 4);
        syscall();
        constant($v0, 10);
        syscall();
        // out of bounds
        dataSection.append("OUT_OF_BOUNDS: .asciiz \"Access Violation\"").append(NEWLINE);
        label(IRContext.STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS);
        loadAddress($a0, new IRLabel("OUT_OF_BOUNDS"));
        constant($v0, 4);
        syscall();
        constant($v0, 10);
        syscall();
        // exit
        label(IRContext.STDLIB_FUNCTION_EXIT);
        constant($v0, 10);
        syscall();
        // malloc
        // TODO add zeroing code
        label(IRContext.STDLIB_FUNCTION_MALLOC);
        pop($a0);
        push($ra);
        constant($v0, 9);
        syscall();
        pop($ra);
        jumpRegister($ra);
        // PrintInt
        label(IRContext.STDLIB_FUNCTION_PRINT_INT);
        pop($a0);
        push($ra);
        constant($v0, 1);
        syscall();
        constant($a0, 32);
        constant($v0, 11);
        syscall();
        pop($ra);
        jumpRegister($ra);
        // PrintString
        label(IRContext.STDLIB_FUNCTION_PRINT_STRING);
        pop($a0);
        push($ra);
        constant($v0, 4);
        syscall();
        constant($a0, 32);
        constant($v0, 11);
        syscall();
        pop($ra);
        jumpRegister($ra);
    }
    //endregion

    //region Flow commands
    private void gotoCommand(IRGotoCommand command) {
        jump(command.getLabel());
    }

    private void ifnzCommand(IRIfNotZeroCommand command) {
        int register = MR_prepareRegister(command.condition);
        branchNotEqual(register, $0, command.getLabel());
    }

    private void ifzCommand(IRIfZeroCommand command) {
        int register = MR_prepareRegister(command.condition);
        branchEqual(register, $0, command.getLabel());
    }
    //endregion

    //region Memory commands
    private void loadCommand(IRLoadCommand command) {
        int source = MR_prepareRegister(command.source);
        loadFromMemory($t8, source);
        MRR_setRegister(command.dest, $t8);
    }

    private void loadAddressFromLabelCommand(IRLoadAddressFromLabelCommand command) {
        int dest = MR_prepareRegister(command.dest);
        loadAddress(dest, command.label);
    }

    private void storeCommand(IRStoreCommand command) {
        int source = MR_prepareRegister(command.source);
        if (!isSafeRegister(command.dest)) {
            move($t9, source);
            source = $t9;
        }
        int dest = MR_prepareRegister(command.dest);
        storeToMemory(dest, source);
    }
    //endregion

    //region Arithmetic commands
    private void binOpCommand(IRBinOpCommand command) {
        int left = MR_prepareRegister(command.first);
        if (!isSafeRegister(command.second)) {
            move($t9, left);
            left = $t9;
        }
        int right = MR_prepareRegister(command.second);
        binOp($t9, left, command.op, right);
        MRR_setRegister(command.dest, $t9);
    }

    private void binOpRightConstCommand(IRBinOpRightConstCommand command) {
        int left = MR_prepareRegister(command.first);
        constant($t9, command.second);
        binOp($t9, left, command.op, $t9);
        MRR_setRegister(command.dest, $t9);
    }

    private void constCommand(IRConstCommand command) {
        constant($t8, command.value);
        MRR_setRegister(command.dest, $t8);
    }

    private void setValueCommand(IRSetValueCommand command) {
        int source = MR_prepareRegister(command.source);
        MRR_setRegister(command.dest, source);
    }
    //endregion

    //region Function Command
    private void callCommand(IRCallCommand command) {
        jumpAndLink(command.label);
    }

    private void callRegisterCommand(IRCallRegisterCommand command) {
        int reg = MR_prepareRegister(command.function);
        jumpRegisterAndLink(reg);
    }

    private void popCommand(IRPopCommand command) {
        MRR_setRegister(command.dest, $v0);
    }

    private void pushCommand(IRPushCommand command) {
        int reg = MR_prepareRegister(command.source);
        push(reg);
    }
    //endregion

    //region Codegen

    /**
     * Can use the register without overriding $t8
     */
    private boolean isSafeRegister(Register register) {
        return localRegisters.containsKey(register);
    }

    /**
     * Returns the real register the data is stored on, or load it to $t8.
     */
    private int MR_prepareRegister(Register register) {
        // all those loads modify $t8, but then in the end store to it, so it's ok.
        if (isSafeRegister(register)) {
            return localRegisters.get(register);
        } else if (register instanceof TempRegister) {
            // register is not actually used, otherwise it'll be safe
            return $t8;
        } else if (register instanceof ParameterRegister) {
            MR_loadParam($t8, register.getId(), parametersCount);
        } else if (register instanceof LocalRegister) {
            MR_loadLocal($t8, register.getId());
        } else if (register instanceof ThisRegister) {
            MR_loadThis($t8, parametersCount);
        } else if (register instanceof ReturnRegister) {
            MR_loadLocal($t8, localsCount);
        } else if (register instanceof GlobalRegister) {
            loadGlobalVariable($t8, register);
        } else {
            throw new IllegalArgumentException("cannot handle this type of register: " + register.getClass().getSimpleName());
        }
        return $t8;
    }

    private void MRR_setRegister(Register dest, int src) {
        if (isSafeRegister(dest)) {
            int realRegister = localRegisters.get(dest);
            move(realRegister, src);
        } else {
            move($t9, src);
            if (dest instanceof TempRegister) {
                // if temporary register is not safe, then it is not actually used in the program
                // moving to $t8 so generated code make sense
                move($t8, $t9);
            } else if (dest instanceof ParameterRegister) {
                MR_storeParam($t9, dest.getId(), parametersCount);
            } else if (dest instanceof LocalRegister) {
                MR_storeLocal($t9, dest.getId());
            } else if (dest instanceof ThisRegister) {
                MR_storeThis($t9, parametersCount);
            } else if (dest instanceof ReturnRegister) {
                MR_storeLocal($t9, localsCount);
            } else if (dest instanceof GlobalRegister) {
                storeGlobalVariable($t9, dest);
            } else {
                throw new IllegalArgumentException("cannot handle this type of register: " + dest.getClass().getSimpleName());
            }
        }
    }

    private int localOffset(int id) {
        return -(id * SIZE) - SIZE;
    }

    private int parameterOffset(int id, int parametersCount) {
        // id is zero based, so need to increase it by 1
        return REGISTERS_BACKUP_SIZE + SKIP_SIZE + ((parametersCount - 1) - id) * SIZE;
    }

    private String name(int reg) {
        return registerNames.get(reg);
    }

    private void loadGlobalVariable(int dest, Register register) {
        loadFromMemory(dest, globals.get(register));
    }

    private void storeGlobalVariable(int src, Register register) {
        storeToMemory(src, globals.get(register));
    }

    private void MR_loadLocal(int dest, int id) {
        MR_loadOffsetedVariable(dest, localOffset(id));
    }

    private void MR_loadParam(int dest, int id, int parametersCount) {
        MR_loadOffsetedVariable(dest, parameterOffset(id, parametersCount));
    }

    private void MR_loadThis(int dest, int parametersCount) {
        MR_loadOffsetedVariable(dest, parameterOffset(0, parametersCount));
    }

    private void MR_loadOffsetedVariable(int dest, int offset) {
        constant($t8, offset);
        selfAddReg($t8, $fp);
        loadFromMemory(dest, $t8);
    }

    private void MR_storeLocal(int srcReg, int id) {
        MR_storeOffsetedVariable(srcReg, localOffset(id));
    }

    private void MR_storeParam(int srcReg, int id, int parametersCount) {
        MR_storeOffsetedVariable(srcReg, parameterOffset(id, parametersCount));
    }

    private void MR_storeThis(int srcReg, int parametersCount) {
        MR_storeOffsetedVariable(srcReg, parameterOffset(0, parametersCount));
    }

    private void MR_storeOffsetedVariable(int srcReg, int offset) {
        constant($t8, offset);
        selfAddReg($t8, $fp);
        storeToMemory($t8, srcReg);
    }

    private void syscall() {
        codeSection.append(INDENTATION).append("syscall").append(NEWLINE);
    }

    private void binOp(int dest, int leftReg, Operation op, int rightReg) {
        switch (op) {
            case Plus:
                codeSection.append(INDENTATION).append("add ").append(name(dest)).append(",").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE);
                break;
            case Minus:
                codeSection.append(INDENTATION).append("sub ").append(name(dest)).append(",").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE);
                break;
            case Times:
                codeSection.append(INDENTATION).append("mult ").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE)
                        .append(INDENTATION).append("mflo ").append(name(dest)).append(NEWLINE);
                break;
            case Divide:
                codeSection.append(INDENTATION).append("div ").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE)
                        .append(INDENTATION).append("mflo ").append(name(dest)).append(NEWLINE);
                break;
            case Equals:
                // https://stackoverflow.com/questions/22307700/mips-branch-testing-equality
                //  # $t2 will be 0 if $t0 and $t1 are equal, and non-zero otherwise
                //  subu $t2, $t0, $t1
                //  # Set $t2 to 1 if it's non-zero
                //  sltu $t2, $zero, $t2
                //  # Flip the lsb so that 0 becomes 1, and 1 becomes 0
                //  xori $t2, $t2, 1
                codeSection.append(INDENTATION).append("subu ").append(name(dest)).append(",").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE);
                codeSection.append(INDENTATION).append("sltu ").append(name(dest)).append(",").append(name($0)).append(",").append(name(dest)).append(NEWLINE);
                codeSection.append(INDENTATION).append("xori ").append(name(dest)).append(",").append(name(dest)).append(",1").append(NEWLINE);
                break;
            case GreaterThan:
                codeSection.append(INDENTATION).append("slt ").append(name(dest)).append(",").append(name(rightReg)).append(",").append(name(leftReg)).append(NEWLINE);
                break;
            case LessThan:
                codeSection.append(INDENTATION).append("slt ").append(name(dest)).append(",").append(name(leftReg)).append(",").append(name(rightReg)).append(NEWLINE);
                break;
            case Concat:
                //FIXME implement string concat
                break;
            case StrEquals:
                // FIXME implement string equality
                break;
        }
    }

    private void label(IRLabel label) {
        codeSection.append(label).append(':').append(NEWLINE);
    }

    private void push(int register) {
        selfAddConst($sp, -SIZE);
        storeToMemory($sp, register);
    }

    private void pushConst(int constant) {
        selfAddConst($sp, -SIZE);
        constant($t8, constant);
        storeToMemory($sp, $t8);
    }


    private void pop(int register) {
        loadFromMemory(register, $sp);
        selfAddConst($sp, SIZE);
    }

    private void loadFromMemory(int dest, int memRegister) {
        codeSection.append(INDENTATION).append("lw ").append(name(dest)).append(",(").append(name(memRegister)).append(")").append(NEWLINE);
    }

    private void loadFromMemory(int dest, IRLabel label) {
        codeSection.append(INDENTATION).append("lw ").append(name(dest)).append(",").append(label).append(NEWLINE);
    }

    private void loadAddress(int dest, IRLabel label) {
        codeSection.append(INDENTATION).append("la ").append(name(dest)).append(",").append(label).append(NEWLINE);
    }

    private void storeToMemory(int memoryDest, int register) {
        codeSection.append(INDENTATION).append("sw ").append(name(register)).append(",(").append(name(memoryDest)).append(")").append(NEWLINE);
    }

    private void storeToMemory(int memoryDest, IRLabel label) {
        codeSection.append(INDENTATION).append("lw ").append(name(memoryDest)).append(",").append(label).append(NEWLINE);
    }

    private void move(int to, int from) {
        codeSection.append(INDENTATION).append("move ").append(name(to)).append(",").append(name(from)).append(NEWLINE);
    }

    private void constant(int reg, int constant) {
        codeSection.append(INDENTATION).append("addi ").append(name(reg)).append(",").append(name($0)).append(",").append(constant).append(NEWLINE);
    }

    private void selfAddConst(int reg, int constant) {
        codeSection.append(INDENTATION).append("addi ").append(name(reg)).append(",").append(name(reg)).append(",").append(constant).append(NEWLINE);
    }

    private void selfAddReg(int reg, int registerToAdd) {
        codeSection.append(INDENTATION).append("add ").append(name(reg)).append(",").append(name(reg)).append(",").append(name(registerToAdd)).append(NEWLINE);
    }

    private void jumpRegister(int reg) {
        codeSection.append(INDENTATION).append("jr ").append(name(reg)).append(NEWLINE);
    }

    private void jumpRegisterAndLink(int reg) {
        codeSection.append(INDENTATION).append("jalr ").append(name(reg)).append(NEWLINE);
    }

    private void jump(IRLabel label) {
        codeSection.append(INDENTATION).append("j ").append(label).append(NEWLINE);
    }

    private void jumpAndLink(IRLabel label) {
        codeSection.append(INDENTATION).append("jal ").append(label).append(NEWLINE);
    }

    private void branchNotEqual(int reg1, int reg2, IRLabel label) {
        codeSection.append(INDENTATION).append("bne ").append(name(reg1)).append(",").append(name(reg2)).append(",").append(label).append(NEWLINE);
    }

    private void branchEqual(int reg1, int reg2, IRLabel label) {
        codeSection.append(INDENTATION).append("beq ").append(name(reg1)).append(",").append(name(reg2)).append(",").append(label).append(NEWLINE);
    }

    private void comment(String s) {
        codeSection.append(INDENTATION).append("# ").append(s).append(NEWLINE);
    }
    //endregion
}
