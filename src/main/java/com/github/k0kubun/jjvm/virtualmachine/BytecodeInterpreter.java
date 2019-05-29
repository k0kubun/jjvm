package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.Instruction;
import com.github.k0kubun.jjvm.classfile.Instruction.Opcode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class BytecodeInterpreter {
    private final ClassFile klass;
    private int pc; // program counter
    private final Deque<Object> stack;

    public BytecodeInterpreter(ClassFile klass) {
        this.klass = klass;
        this.stack = new ArrayDeque<>();
        this.pc = 0;
    }

    public void execute(AttributeInfo.Code code) {
        List<Instruction> instructions = code.getInstructions();
        while (true) {
            Instruction instruction = instructions.get(pc);
            Opcode opcode = instruction.getOpcode();

            if (opcode == Opcode.Getstatic) {
                System.out.println("getstatic");
            } else if (opcode == Opcode.Ldc) {
                System.out.println("ldc");
            } else if (opcode == Opcode.Invokevirtual) {
                System.out.println("invokevirtual");
            } else if (opcode == Opcode.Return) {
                return;
            } else {
                throw new RuntimeException("BytecodeInterpreter#execute does not implement opcode: " + opcode.getName());
            }

            pc++;
        }
    }
}
