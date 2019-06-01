package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.Instruction;
import com.github.k0kubun.jjvm.classfile.Instruction.Opcode;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// The core of the VirtualMachine.
// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
public class BytecodeInterpreter {
    private final ClassFile klass;
    private int pc; // program counter
    private final Deque<ConstantInfo> stack;

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

            switch (opcode) {
                case Getstatic:
                    stack.push(getConstant(instruction.getIndex()));
                    break;
                case Ldc:
                    stack.push(getConstant(instruction.getOperands()[0]));
                    break;
                case Invokevirtual:
                    ConstantInfo.String str = (ConstantInfo.String)stack.pop();
                    ConstantInfo.Utf8 utf8 = (ConstantInfo.Utf8)getConstant(str.getNameIndex());
                    stack.pop(); // receiver
                    System.out.println(utf8.getString()); // TODO: dispatch properly
                    break;
                case Return:
                    return;
                default:
                    throw new RuntimeException("BytecodeInterpreter#execute does not implement opcode: " + opcode.getName());
            }

            pc++;
        }
    }

    private ConstantInfo getConstant(int index) {
        return klass.getConstantPool()[index - 1];
    }
}
