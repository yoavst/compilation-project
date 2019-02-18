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

import java.util.*;
import java.util.stream.Collectors;

import static utils.Utils.isPowerOfTwo;

@SuppressWarnings("SameParameterValue")
public class Mips {
    private static final String INDENTATION = "\t";
    private static final String NEWLINE = "\n";
    private static final int SIZE = IRContext.PRIMITIVE_DATA_SIZE;
    private static final int REGISTERS_COUNT = 8;
    private static final int COLORING_OFFSET = 8;
    private static final int REGISTERS_BACKUP_SIZE = REGISTERS_COUNT * SIZE;
    private static final int SKIP_SIZE = 2 * SIZE;
    // registers
    private static final int $0 = 0;
    private static final int $v0 = 2;
    private static final int $a0 = 4;
    private static final int $a3 = 7;
    private static final int $t0 = 8;
    private static final int $t1 = 9;
    private static final int $t2 = 10;
    private static final int $t8 = 24;
    private static final int $t9 = 25;
    private static final int $sp = 29;
    private static final int $fp = 30;
    private static final int $ra = 31;
    // syscalls
    private static final int SYSCALL_PRINT_INT = 1;
    private static final int SYSCALL_PRINT_STRING = 4;
    private static final int SYSCALL_SBRK = 9;
    private static final int SYSCALL_EXIT = 10;
    private static final int SYSCALL_PRINT_CHAR = 11;
    private static final Map<Integer, String> registerNames = new HashMap<>();
    private static final IRLabel STRING_EQUALS_LABEL = new IRLabel("__string_equality__");
    private static final IRLabel STRING_CONCAT_LABEL = new IRLabel("__string_concat__");
    private static final IRLabel FUNCTION_NAMES = new IRLabel("FUNCTION_NAMES");
    private static final String FUNCTION_NAME_PREFIX = "FUNCTION_NAME_";
    private static final String MAIN_LABEL = "main";

    private StringBuilder dataSection = new StringBuilder();
    private StringBuilder codeSection = new StringBuilder();
    private Map<Register, IRLabel> globals = new HashMap<>();
    private Map<String, Integer> functionIds = new HashMap<>();
    private int functionIdCounter = 0;
    private Map<Register, Integer> realRegMapsRegisters;
    private Map<LocalRegister, Integer> localsAsReal;
    private int parametersCount;
    private int localsCount;
    private int boundedLabelCounter = 0;


    static {
        registerNames.put($0, "$0");
        registerNames.put($a0, "$a0");
        registerNames.put($a3, "$a3");
        registerNames.put($v0, "$v0");
        registerNames.put($t0, "$t0");
        registerNames.put($t1, "$t1");
        registerNames.put($t2, "$t2");
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
        loadVirtualTables(virtualTables);
        loadGlobals(globals);
        jump(new IRLabel(MAIN_LABEL)); // for simulator that does not support main
        functionIds.put(MAIN_LABEL, functionIdCounter++);
        loadStdlib();
        loadCode(commands);
        loadReflectionsInfo();
    }

    public String export() {
        return ".data" + NEWLINE + dataSection + NEWLINE + ".text" + NEWLINE + codeSection + NEWLINE;
    }

    //region Loading
    private void loadConstants(Map<String, IRLabel> constantStrings) {
        constantStrings.forEach((constant, label) -> dataSection.append(label).append(": .asciiz \"").append(constant).append("\"").append(NEWLINE));
    }

    private void loadVirtualTables(Map<IRLabel, List<IRLabel>> virtualTables) {
        virtualTables.forEach((label, methods) ->
                {
                    if (!methods.isEmpty()) {
                        dataSection.append(label)
                                .append(": .word ")
                                .append(methods.stream().map(IRLabel::toString).collect(Collectors.joining(",")))
                                .append(NEWLINE);
                    } else {
                        dataSection.append(label)
                                .append(": .word 0\t# no methods").append(NEWLINE);
                    }
                }
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
        realRegMapsRegisters = new LimitedRegisterAllocator(REGISTERS_COUNT).allocateRealRegister(wholeFunction);
        parametersCount = functionInfo.numberOfParameters;
        localsCount = functionInfo.numberOfLocals;
        saveLocalsAsRealRegisters();
        // update offset of coloring
        realRegMapsRegisters.entrySet().forEach(e -> e.setValue(e.getValue() + COLORING_OFFSET));
        localsAsReal.entrySet().forEach(e -> e.setValue(e.getValue() + COLORING_OFFSET));
        // generate function header
        generateFunctionHeader(functionStartingBlock.label, functionInfo.numberOfParameters, functionInfo.numberOfLocals, functionIds.computeIfAbsent(functionInfo.name, n -> functionIdCounter++));
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

    private void saveLocalsAsRealRegisters() {
        localsAsReal = new HashMap<>();

        int max = realRegMapsRegisters.isEmpty() ? -1 : Collections.max(realRegMapsRegisters.values());
        if (max == (REGISTERS_COUNT - 1) || localsCount == 0)
            return;

        // save the first locals as real register
        int loopEnd = Math.min(REGISTERS_COUNT, max + 1 + localsCount);
        for (int i = max + 1; i < loopEnd; i++) {
            LocalRegister reg = new LocalRegister(i - max - 1);
            realRegMapsRegisters.put(reg, i);
            localsAsReal.put(reg, i);
        }
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
        // set default value to all locals on stack and return value
        if (locals - localsAsReal.size() > 0) {
            comment("Allocating " + (locals - localsAsReal.size()) + " slots for local variables that could not be stored as registers");
            for (int i = 0; i < locals - localsAsReal.size(); i++) {
                pushConst(0);
            }
        }
        comment("Allocating a slot for return value");
        pushConst(0);

        // set default value for all locals on real registers
        comment("Setting initial value to local variables stored as registers");
        comment(localsAsReal.entrySet().stream().map(e -> e.getKey() + " => " + name(e.getValue())).collect(Collectors.joining(", ")));
        localsAsReal.forEach((local, realReg) -> constant(realReg, 0));

        // push function id and header size
        comment("Saving function id and header size");
        pushConst(functionId);
        pushConst((parameters + 2 + locals + 3 - localsAsReal.size()) * SIZE + REGISTERS_BACKUP_SIZE);
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
        selfAddConst($sp, (locals - localsAsReal.size()) * SIZE);
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
        } else if (command instanceof IRPushConstCommand) {
            pushConstCommand(((IRPushConstCommand) command));
        }else if (command instanceof IRLoadCommand) {
            loadCommand(((IRLoadCommand) command));
        } else if (command instanceof IRLoadAddressFromLabelCommand) {
            loadAddressFromLabelCommand(((IRLoadAddressFromLabelCommand) command));
        } else if (command instanceof IRStoreCommand) {
            storeCommand(((IRStoreCommand) command));
        }
    }

    private void loadStdlib() {
        generateDivisionByZeroHandler();
        generateNullPointerHandler();
        generateOutOfBoundsHandler();
        generateExit();
        generatePrintInt();
        generatePrintString();
        generatePrintTrace();
        generateMalloc();
        generateStringEquality();
        generateStringConcat();
    }

    private void loadReflectionsInfo() {
        List<String> names = functionIds.entrySet().stream().sorted(Map.Entry.comparingByValue()).map(Map.Entry::getKey).collect(Collectors.toList());
        List<String> labels = new ArrayList<>(names.size());
        int i = 0;
        for (String name : names) {
            String label = FUNCTION_NAME_PREFIX + i;
            dataSection.append(label).append(": .asciiz \"").append(name).append("\"").append(NEWLINE);
            labels.add(label);
            i++;
        }
        dataSection.append(FUNCTION_NAMES)
                .append(": .word ")
                .append(String.join(",", labels))
                .append(NEWLINE);
    }
    //endregion

    //region Builtins
    private void generateDivisionByZeroHandler() {
        dataSection.append("DIVISION_BY_ZERO: .asciiz \"Division By Zero\"").append(NEWLINE);

        label(IRContext.STDLIB_FUNCTION_THROW_DIVISION_BY_ZERO);
        comment("Stdlib builtin intrinsic");
        syscallPrintString(new IRLabel("DIVISION_BY_ZERO"));
        syscallExit();
    }

    private void generateNullPointerHandler() {
        dataSection.append("NULL_POINTER: .asciiz \"Invalid Pointer Dereference\"").append(NEWLINE);

        label(IRContext.STDLIB_FUNCTION_THROW_NULL);
        comment("Stdlib builtin intrinsic");
        syscallPrintString(new IRLabel("NULL_POINTER"));
        syscallExit();
    }

    private void generateOutOfBoundsHandler() {
        dataSection.append("OUT_OF_BOUNDS: .asciiz \"Access Violation\"").append(NEWLINE);

        label(IRContext.STDLIB_FUNCTION_THROW_OUT_OF_BOUNDS);
        comment("Stdlib builtin intrinsic");
        syscallPrintString(new IRLabel("OUT_OF_BOUNDS"));
        syscallExit();
    }

    private void generateExit() {
        label(IRContext.STDLIB_FUNCTION_EXIT);
        comment("Stdlib builtin intrinsic");
        syscallExit();
    }

    private void generatePrintInt() {
        label(IRContext.STDLIB_FUNCTION_PRINT_INT);
        comment("Stdlib function");

        pop($a0);
        constant($v0, SYSCALL_PRINT_INT);
        syscall();

        syscallPrintChar(' ');

        jumpRegister($ra);
    }

    private void generatePrintString() {
        label(IRContext.STDLIB_FUNCTION_PRINT_STRING);
        comment("Stdlib function");

        pop($a0);
        constant($v0, SYSCALL_PRINT_STRING);
        syscall();

        jumpRegister($ra);
    }

    private void generatePrintTrace() {
        label(IRContext.STDLIB_FUNCTION_PRINT_TRACE);
        comment("Stdlib function");
        // backup registers
        push($t0);
        push($t1);
        push($t2);
        int backupOldSp = $t9;
        move(backupOldSp, $sp);
        selfAddConst($sp, 3 * SIZE);
        // generate loop code
        IRLabel stackUpLoop = new IRLabel("__printTrace_stack_loop__"),
                endLoop = new IRLabel("__printTrace_end_loop__");
        int headerSizeRegister = $t8, functionIdRegister = $t2;

        label(stackUpLoop);
        comment("Read header size and function id");
        pop(headerSizeRegister);
        pop(functionIdRegister);
        comment("Print function name");
        // get offset in $t0
        multiplyByConst($t0, functionIdRegister, SIZE, $t0);
        // get address in $t1
        loadAddress($t1, FUNCTION_NAMES);
        binOp($t1, $t1, Operation.Plus, $t0);
        // load pointer to $a0
        loadFromMemory($a0, $t1);
        // call print inlined
        constant($v0, SYSCALL_PRINT_STRING);
        syscall();

        comment("Check halting case");
        int mainId = functionIds.get(MAIN_LABEL);
        constant($t0, mainId);
        branchEqual(functionIdRegister, $t0, endLoop);

        comment("Jump to next function");
        selfAddConst(headerSizeRegister, -2 * SIZE);
        binOp($sp, $sp, Operation.Plus, headerSizeRegister);
        jump(stackUpLoop);

        label(endLoop);
        // restore registers
        move($sp, backupOldSp);
        pop($t2);
        pop($t1);
        pop($t0);
        jumpRegister($ra);
    }

    private void generateMalloc() {
        label(IRContext.STDLIB_FUNCTION_MALLOC);
        comment("Stdlib builtin intrinsic");

        pop($t8); // backup length

        move($a0, $t8);
        constant($v0, SYSCALL_SBRK);
        syscall();

        // now need to zero space
        IRLabel cond = new IRLabel("__malloc_zero_condition__");
        IRLabel after = new IRLabel("__malloc_zero_after__");

        selfAddConst($v0, -SIZE);
        binOp($t8, $v0, Operation.Plus, $t8);

        label(cond);
        branchEqual($v0, $t8, after);

        storeToMemory($t8, $0);

        selfAddConst($t8, -SIZE);
        jump(cond);
        label(after);

        selfAddConst($v0, SIZE);
        jumpRegister($ra);
    }

    private void generateStringEquality() {
        label(STRING_EQUALS_LABEL);
        comment("Stdlib builtin intrinsic");
        IRLabel loopLabel = new IRLabel("__str_cmp_loop__"),
                equalLabel = new IRLabel("__str_cmp_equal__"),
                notEqualLabel = new IRLabel("__str_cmp_not_equal__"),
                afterLabel = new IRLabel("__str_cmp_after__");
        // https://stackoverflow.com/a/36296825/4874829
        //        # string compare loop (just like strcmp)
        //        cmploop:
        //        lb      $t2,($s2)                   # get next char from str1
        //        lb      $t3,($s3)                   # get next char from str2
        //        bne     $t2,$t3,cmpne               # are they different? if yes, fly
        //        beq     $t2,$zero,cmpeq             # at EOS? yes, fly (strings equal)
        //        addi    $s2,$s2,1                   # point to next char
        //        addi    $s3,$s3,1                   # point to next char
        //        j       cmploop

        int firstString = $t8;
        int secondString = $t9;
        // get two loaded strings
        pop(secondString);
        pop(firstString);
        // need two more registers, backup $t0, $t1 so they could be used
        push($t0);
        push($t1);

        comment("check nullability");
        IRLabel firstNotNull = new IRLabel("__str_cmp_first_null__"),
                secondNotNull = new IRLabel("__str_cmp_second_not_null__");

        constant($t0, IRContext.NIL_VALUE);
        branchNotEqual(firstString, $t0, firstNotNull);
        // first string is null, return whether the second is also null.
        binOp($v0, secondString, Operation.Equals, $t0);
        jump(afterLabel);

        label(firstNotNull);
        // first is not null, check about the second
        branchNotEqual(secondString, $t0, secondNotNull);
        // second string is null, return false
        move($v0, $0);
        jump(afterLabel);
        label(secondNotNull);

        comment("check equality");
        label(loopLabel);
        loadByteFromMemory($t0, firstString);
        loadByteFromMemory($t1, secondString);
        branchNotEqual($t0, $t1, notEqualLabel);
        branchEqual($t0, $0, equalLabel);
        selfAddConst(firstString, 1);
        selfAddConst(secondString, 1);
        jump(loopLabel);
        // equal case
        label(equalLabel);
        constant($v0, 1);
        jump(afterLabel);
        // not equal case
        label(notEqualLabel);
        move($v0, $0);
        jump(afterLabel);
        // restore original registers
        label(afterLabel);
        pop($t1);
        pop($t0);
        jumpRegister($ra);


    }

    private void generateStringConcat() {
        label(STRING_CONCAT_LABEL);
        comment("Stdlib builtin intrinsic");

        int firstString = $t8;
        int secondString = $t9;
        int lengthField = $t0;

        // get two loaded strings
        pop(secondString);
        pop(firstString);
        // need more registers, backup so they could be used
        push($t0);
        push($t1);
        push($t2);

        IRLabel firstStringLengthLoop = new IRLabel("__str_concat_first_length__"),
                afterFirstStringLengthLoop = new IRLabel("__str_concat_first_length_after__"),
                secondStringLengthLoop = new IRLabel("__str_concat_second_length__"),
                afterSecondStringLengthLoop = new IRLabel("__str_concat_second_length_after__");

        comment("calculate length of first string");
        move(lengthField, $0);
        move($t1, firstString);

        label(firstStringLengthLoop);
        loadByteFromMemory($t2, $t1);
        branchEqual($t2, $0, afterFirstStringLengthLoop);
        selfAddConst(lengthField, 1);
        selfAddConst($t1, 1);
        jump(firstStringLengthLoop);

        label(afterFirstStringLengthLoop);
        comment("calculate length of second string");
        move($t1, secondString);
        label(secondStringLengthLoop);
        loadByteFromMemory($t2, $t1);
        branchEqual($t2, $0, afterSecondStringLengthLoop);
        selfAddConst(lengthField, 1);
        selfAddConst($t1, 1);
        jump(secondStringLengthLoop);

        label(afterSecondStringLengthLoop);

        comment("allocate needed space for concatenation - round of to multiplication of 4");
        selfAddConst(lengthField, 5);
        andConst(lengthField, lengthField, "0xFFFC");
        push($ra);
        push(firstString);
        push(secondString);
        push(lengthField);

        push(lengthField);
        jumpAndLink(IRContext.STDLIB_FUNCTION_MALLOC);

        pop(lengthField);
        pop(secondString);
        pop(firstString);
        pop($ra);

        comment("write to allocation");
        IRLabel writeFirst = new IRLabel("__str_concat_first_write__"),
                afterFirst = new IRLabel("__str_concat_first_after_write__"),
                writeSecond = new IRLabel("__str_concat_second_write__"),
                afterSecond = new IRLabel("__str_concat_second_after_write__");

        int currentPointer = $t2; // can reuse length field since not used anymore

        move(currentPointer, $v0);
        label(writeFirst);
        loadByteFromMemory($t1, firstString);
        branchEqual($t1, $0, afterFirst);

        storeByteToMemory(currentPointer, $t1);
        selfAddConst(currentPointer, 1);
        selfAddConst(firstString, 1);
        jump(writeFirst);
        label(afterFirst);

        label(writeSecond);
        loadByteFromMemory($t1, secondString);
        branchEqual($t1, $0, afterSecond);

        storeByteToMemory(currentPointer, $t1);
        selfAddConst(currentPointer, 1);
        selfAddConst(secondString, 1);
        jump(writeSecond);
        label(afterSecond);
        // append zero
        storeByteToMemory(currentPointer, $0);

        comment("restore original registers");
        pop($t2);
        pop($t1);
        pop($t0);
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
        if (!isSafeRegister(command.dest) && source == $t8) {
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

        if (isSafeRegister(command.second)) {
            if (isSafeRegister(command.dest)) {
                // dest and right are safe, can perform operation directly.
                binOp(MR_prepareRegister(command.dest), left, command.op, MR_prepareRegister(command.second));
            } else {
                // dest is not safe, need to save to temp
                binOp($t9, left, command.op, MR_prepareRegister(command.second));
                MRR_setRegister(command.dest, $t9);
            }
        } else {
            // right is not safe, need to make sure left is not getting override
            int right;
            if (left == $t8) {
                right = prepareRegister(command.second, $t9);
            } else {
                right = prepareRegister(command.second, $t9);
            }

            if (isSafeRegister(command.dest)) {
                binOp(MR_prepareRegister(command.dest), left, command.op, right);
            } else {
                binOp($t9, left, command.op, right);
                MRR_setRegister(command.dest, $t9);
            }
        }
    }

    private void binOpRightConstCommand(IRBinOpRightConstCommand command) {
        int left = MR_prepareRegister(command.first);
        int dest = isSafeRegister(command.dest) ? MR_prepareRegister(command.dest) : $t9;
        switch (command.op) {
            case Plus:
                addConst(dest, left, command.second);
                break;
            case Minus:
                addConst(dest, left, -command.second);
                break;
            case LessThan:
                setOnLessThanConst(dest, left, command.second);
                break;
            case Times:
                multiplyByConst(dest, left, command.second, $t9);
                break;
            case Equals:
                equalToConst(dest, left, command.second);
                break;
            case BoundedPlus:
                addConst(dest, left, command.second);
                coerce(dest);
                break;
            case BoundedMinus:
                addConst(dest, left, -command.second);
                coerce(dest);
                break;
            case BoundedTimes:
                multiplyByConst(dest, left, command.second, $t9);
                coerce(dest);
                break;
            case BoundedDivide:
            case Divide:
            case GreaterThan:
            case Concat:
            case StrEquals:
                constant($t9, command.second);
                binOp(dest, left, command.op, $t9);
                break;
        }
        if (!isSafeRegister(command.dest))
            MRR_setRegister(command.dest, dest);
    }

    private void constCommand(IRConstCommand command) {
        if (isSafeRegister(command.dest)) {
            constant(MR_prepareRegister(command.dest), command.value);
        } else {
            constant($t8, command.value);
            MRR_setRegister(command.dest, $t8);
        }
    }

    private void setValueCommand(IRSetValueCommand command) {
        if (isSafeRegister(command.dest)) {
            if (isSafeRegister(command.source)) {
                move(MR_prepareRegister(command.dest), MR_prepareRegister(command.source));
            } else {
                // directly load it dest
                prepareRegister(command.source, MR_prepareRegister(command.dest));
            }
        } else {
            int source = MR_prepareRegister(command.source);
            MRR_setRegister(command.dest, source);
        }
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
        // if the dest is temporary and was not allocated a register, then it is an assignment to a register which will never be alive.
        // therefore, we will not have to generate a call.
        if (!command.dest.isTemporary() || isSafeRegister(command.dest)) {
            MRR_setRegister(command.dest, $v0);
        }
    }

    private void pushCommand(IRPushCommand command) {
        int reg = MR_prepareRegister(command.source);
        push(reg);
    }

    private void pushConstCommand(IRPushConstCommand command) {
        pushConst(command.constant);
    }
    //endregion

    //region Codegen

    /**
     * Can use the register without overriding $t8 - calling {@link #MR_prepareRegister(Register)} will immediately return a real register
     */
    private boolean isSafeRegister(Register register) {
        return realRegMapsRegisters.containsKey(register);
    }

    /**
     * Returns the real register the data is stored on, or load it to $t8.
     */
    private int MR_prepareRegister(Register register) {
        return prepareRegister(register, $t8);
    }

    /**
     * Returns the real register the data is stored on, or load it to temp.
     */
    private int prepareRegister(Register register, int temp) {
        // all those loads modify $t8, but then in the end store to it, so it's ok.
        if (isSafeRegister(register)) {
            return realRegMapsRegisters.get(register);
        } else if (register instanceof TempRegister) {
            // register is not actually used, otherwise it'll be safe
            return temp;
        } else if (register instanceof ParameterRegister) {
            loadParam(temp, register.getId(), parametersCount);
        } else if (register instanceof LocalRegister) {
            loadLocal(temp, register.getId());
        } else if (register instanceof ThisRegister) {
            loadThis(temp, parametersCount);
        } else if (register instanceof ReturnRegister) {
            loadLocal(temp, localsCount);
        } else if (register instanceof GlobalRegister) {
            loadGlobalVariable(temp, register);
        } else {
            throw new IllegalArgumentException("cannot handle this type of register: " + register.getClass().getSimpleName());
        }
        return temp;
    }

    private void MRR_setRegister(Register dest, int src) {
        if (isSafeRegister(dest)) {
            int realRegister = realRegMapsRegisters.get(dest);
            if (realRegister != src)
                move(realRegister, src);
        } else {
            if (src == $t8) {
                move($t9, src);
                src = $t9;
            }
            //noinspection StatementWithEmptyBody
            if (dest instanceof TempRegister) {
                // not colored => not alive
                // ignored
            } else if (dest instanceof ParameterRegister) {
                MR_storeParam(src, dest.getId(), parametersCount);
            } else if (dest instanceof LocalRegister) {
                MR_storeLocal(src, dest.getId());
            } else if (dest instanceof ThisRegister) {
                MR_storeThis(src, parametersCount);
            } else if (dest instanceof ReturnRegister) {
                MR_storeLocal(src, localsCount);
            } else if (dest instanceof GlobalRegister) {
                storeGlobalVariable(src, dest);
            } else {
                throw new IllegalArgumentException("cannot handle this type of register: " + dest.getClass().getSimpleName());
            }
        }
    }

    private int localOffset(int id) {
        return -(id * SIZE) - SIZE + (localsAsReal.size() * SIZE);
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

    private void loadLocal(int dest, int id) {
        loadOffsetedVariable(dest, localOffset(id));
    }

    private void loadParam(int dest, int id, int parametersCount) {
        loadOffsetedVariable(dest, parameterOffset(id, parametersCount));
    }

    private void loadThis(int dest, int parametersCount) {
        loadOffsetedVariable(dest, parameterOffset(0, parametersCount));
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

    private void loadOffsetedVariable(int dest, int offset) {
        addConst(dest, $fp, offset);
        loadFromMemory(dest, dest);
    }

    private void MR_storeOffsetedVariable(int srcReg, int offset) {
        addConst($t8, $fp, offset);
        storeToMemory($t8, srcReg);
    }

    private void move(int to, int from) {
        codeSection.append(INDENTATION).append("move ").append(name(to)).append(",").append(name(from)).append(NEWLINE);
    }

    private void comment(String s) {
        codeSection.append(INDENTATION).append("# ").append(s).append(NEWLINE);
    }
    //endregion

    //region Codegen arithmetic
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
                push(leftReg);
                push(rightReg);
                jumpAndLink(STRING_CONCAT_LABEL);
                move(dest, $v0);
                break;
            case StrEquals:
                push(leftReg);
                push(rightReg);
                jumpAndLink(STRING_EQUALS_LABEL);
                move(dest, $v0);
                break;
            case BoundedPlus:
                binOp(dest, leftReg, Operation.Plus, rightReg);
                coerce(dest);
                break;
            case BoundedMinus:
                binOp(dest, leftReg, Operation.Minus, rightReg);
                coerce(dest);
                break;
            case BoundedTimes:
                binOp(dest, leftReg, Operation.Times, rightReg);
                coerce(dest);
                break;
            case BoundedDivide:
                binOp(dest, leftReg, Operation.Divide, rightReg);
                coerce(dest);
                break;
        }
    }

    private void coerce(int dest) {
        IRLabel coerceDown = new IRLabel("__arithmetic_coerce_down_" + boundedLabelCounter++ + "__"),
                coerceUp = new IRLabel("__arithmetic_coerce_up_" + boundedLabelCounter++ + "__"),
                ok = new IRLabel("__arithmetic_continue_" + boundedLabelCounter++ + "__");

        comment("[Start] Poseidon arithmetic handling");
        setOnLessThanConst($a3, dest, IRContext.MIN_INT);                                       // temp = result < MIN_INT
        branchNotEqual($a3, $0, coerceUp);                                                      // if temp != 0 goto coerceUp
        binOp($a3, $0, Operation.Minus, dest);                                                  // temp = -result
        setOnLessThanConst($a3, $a3, -IRContext.MAX_INT);                                       // temp = (-result) < (-MAX_INT)
        branchNotEqual($a3, $0, coerceDown);                                                    // if temp != 0 goto coerceDown

        jump(ok);                                                                               // goto ok

        label(coerceDown);
        constant(dest, IRContext.MAX_INT);
        jump(ok);

        label(coerceUp);
        constant(dest, IRContext.MIN_INT);
        label(ok);
        comment("[End] Poseidon arithmetic handling");
    }

    private void equalToConst(int dest, int reg, int constant) {
        // see binOp(...) for explanation
        // https://stackoverflow.com/a/36274750/4874829
        // Despite its name, add immediate unsigned (addiu) is used to add constants to signed integers when we don't care about overflow.
        // MIPS has no subtract immediate instruction, and negative numbers need sign extension, so the MIPS architects decided to sign-extend the immediate field.
        codeSection.append(INDENTATION).append("addiu  ").append(name(dest)).append(",").append(name(reg)).append(",").append(-constant).append(NEWLINE);
        codeSection.append(INDENTATION).append("sltu ").append(name(dest)).append(",").append(name($0)).append(",").append(name(dest)).append(NEWLINE);
        codeSection.append(INDENTATION).append("xori ").append(name(dest)).append(",").append(name(dest)).append(",1").append(NEWLINE);
    }

    private void multiplyByConst(int dest, int reg, int constant, int fallbackRegister) {
        // Arithmetic left shifts are equivalent to multiplication by a (positive, integral) power of the radix (e.g., a multiplication by a power of 2 for binary numbers)
        if (constant != 1) {
            if (constant == 0) {
                move(dest, $0);
            } else if (isPowerOfTwo(constant)) {
                // implemented as shift left
                codeSection.append(INDENTATION).append("sll ").append(name(dest)).append(",").append(name(reg)).append(",").append(Integer.numberOfTrailingZeros(constant)).append(NEWLINE);
            } else {
                // fallback to normal approach
                constant(fallbackRegister, constant);
                binOp(dest, reg, Operation.Times, fallbackRegister);
            }
        }
    }

    private void setOnLessThanConst(int dest, int reg, int constant) {
        codeSection.append(INDENTATION).append("slti ").append(name(dest)).append(",").append(name(reg)).append(",").append(constant).append(NEWLINE);
    }

    private void addConst(int dest, int reg, int constant) {
        if (constant != 0)
            codeSection.append(INDENTATION).append("addi ").append(name(dest)).append(",").append(name(reg)).append(",").append(constant).append(NEWLINE);
        else if (dest != reg)
            move(dest, reg);

    }

    private void andConst(int dest, int reg, String integerConstant) {
        codeSection.append(INDENTATION).append("andi ").append(name(dest)).append(",").append(name(reg)).append(",").append(integerConstant).append(NEWLINE);
    }

    private void constant(int reg, int constant) {
        if (constant == 0) {
            move(reg, $0);
        } else {
            codeSection.append(INDENTATION).append("addi ").append(name(reg)).append(",").append(name($0)).append(",").append(constant).append(NEWLINE);
        }
    }


    private void selfAddConst(int reg, int constant) {
        addConst(reg, reg, constant);
    }
    //endregion

    //region Codegen memory access
    private void loadAddress(int dest, IRLabel label) {
        codeSection.append(INDENTATION).append("la ").append(name(dest)).append(",").append(label).append(NEWLINE);
    }

    private void loadByteFromMemory(int dest, int memRegister) {
        codeSection.append(INDENTATION).append("lb ").append(name(dest)).append(",(").append(name(memRegister)).append(")").append(NEWLINE);
    }

    private void storeByteToMemory(int memoryDest, int register) {
        codeSection.append(INDENTATION).append("sb ").append(name(register)).append(",(").append(name(memoryDest)).append(")").append(NEWLINE);
    }

    private void loadFromMemory(int dest, int memRegister) {
        codeSection.append(INDENTATION).append("lw ").append(name(dest)).append(",(").append(name(memRegister)).append(")").append(NEWLINE);
    }

    private void loadFromMemory(int dest, IRLabel label) {
        codeSection.append(INDENTATION).append("lw ").append(name(dest)).append(",").append(label).append(NEWLINE);
    }

    private void storeToMemory(int memoryDest, int register) {
        codeSection.append(INDENTATION).append("sw ").append(name(register)).append(",(").append(name(memoryDest)).append(")").append(NEWLINE);
    }

    private void storeToMemory(int memoryDest, IRLabel label) {
        codeSection.append(INDENTATION).append("sw ").append(name(memoryDest)).append(",").append(label).append(NEWLINE);
    }
    //endregion

    //region Codegen stack
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
    //endregion

    //region Codegen flow
    private void label(IRLabel label) {
        codeSection.append(label).append(':').append(NEWLINE);
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
    //endregion

    //region Codegen syscall
    private void syscall() {
        codeSection.append(INDENTATION).append("syscall").append(NEWLINE);
    }

    private void syscallPrintString(IRLabel label) {
        loadAddress($a0, label);
        constant($v0, SYSCALL_PRINT_STRING);
        syscall();
    }

    private void syscallExit() {
        constant($v0, SYSCALL_EXIT);
        syscall();
    }

    private void syscallPrintChar(char c) {
        constant($a0, (int) c);
        constant($v0, SYSCALL_PRINT_CHAR);
        syscall();
    }
    //endregion
}
