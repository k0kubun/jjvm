package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser.DescriptorParser;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.Instruction.Opcode;
import com.github.k0kubun.jjvm.classfile.Instruction;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

// The core of the VirtualMachine.
// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
public class BytecodeInterpreter {
    private final VirtualMachine vm;
    private final Value.Class klass;
    private int pc; // program counter
    private final Deque<Value> stack;

    public BytecodeInterpreter(VirtualMachine vm, Value.Class klass) {
        this.vm = vm;
        this.klass = klass;
        this.stack = new ArrayDeque<>();
        this.pc = 0;
    }

    public void execute(AttributeInfo.Code code, Value[] methodArgs) {
        Value[] locals = new Value[code.getMaxLocals()];
        for (int i = 0; i < methodArgs.length; i++) {
            locals[i] = methodArgs[i];
        }
        List<Instruction> instructions = code.getInstructions();

        while (true) {
            Instruction instruction = instructions.get(pc);
            Opcode opcode = instruction.getOpcode();

            switch (opcode) {
                // case Nop:
                // case Aconst_Null:
                // case Iconst_M1:
                // case Iconst_0:
                // case Iconst_1:
                // case Bipush:
                // case Sipush:
                case Ldc:
                    ConstantInfo constValue = getConstant(instruction.getOperands()[0]);
                    if (constValue instanceof ConstantInfo.String) {
                        FieldType type = DescriptorParser.parseField("Ljava/lang/String;");
                        stack.push(new Value(type,
                                ((ConstantInfo.Utf8)getConstant(((ConstantInfo.String) constValue).getNameIndex())).getString()));
                    } else {
                        throw new RuntimeException("Unexpected ConstantType in ldc: " + constValue.getType());
                    }
                    break;
                // case Aload:
                // case Iload_0:
                // case Iload_1:
                // case Iload_2:
                // case Iload_3:
                // case Lload_1:
                // case Fload_1:
                // case Dload_1:
                // case Dload_2:
                case Aload_0:
                    stack.push(locals[0]);
                    break;
                case Aload_1:
                    stack.push(locals[1]);
                    break;
                // case Aload_2:
                // case Aload_3:
                // case Caload:
                // case Istore_3:
                // case Astore_0:
                case Astore_1:
                    locals[1] = stack.pop();
                    break;
                case Astore_2:
                    locals[2] = stack.pop();
                    break;
                // case Astore_3:
                // case Astore:
                // case Pop:
                case Dup:
                    stack.push(stack.getFirst());
                    break;
                // case Ior:
                // case Iinc:
                // case Ifeq:
                // case Ifne:
                // case Iflt:
                // case IfIcmpeq:
                // case IfIcmpge:
                // case IfAcmpeq:
                // case Goto:
                // case Ireturn:
                // case Areturn:
                case Return:
                    return;
                case Getstatic:
                    // TODO: fetch from actual static field
                    ConstantInfo.Fieldref value = (ConstantInfo.Fieldref)getConstant(instruction.getIndex()); // not used yet. TODO: switch on constant type for cast
                    ConstantInfo.NameAndType nameAndType = (ConstantInfo.NameAndType)getConstant(value.getNameAndTypeIndex());
                    FieldType type = DescriptorParser.parseField(((ConstantInfo.Utf8)getConstant(nameAndType.getDescriptorIndex())).getString());
                    stack.push(new Value(type, value));
                    break;
                // case Putstatic:
                // case Getfield:
                // case Putfield:
                case Invokevirtual:
                    String str = (String)stack.pop().getValue();
                    stack.pop(); // recever
                    System.out.println(str);
                    break;
                    // fallthrough
                case Invokespecial:
                    // TODO: handle `protected` specially
                    invokeMethod(instruction.getIndex());
                    break;
                // case Invokestatic:
                // case Invokeinterface:
                // case New:
                // case Arraylength:
                // case Athrow:
                // case Checkcast:
                // case Instanceof:
                case Monitorenter:
                    stack.pop(); // TODO: synchronize this
                    break;
                // case Monitorexit:
                // case Ifnull:
                case Ifnonnull:
                    if (stack.pop().getValue() != null) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                default:
                    throw new RuntimeException("BytecodeInterpreter#execute does not implement opcode: " + opcode.getName());
            }

            pc++;
        }
    }

    private void invokeMethod(int methodIndex) {
        ConstantInfo.Methodref methodref = (ConstantInfo.Methodref)getConstant(methodIndex);
        ConstantInfo.NameAndType nameAndType = (ConstantInfo.NameAndType)getConstant(methodref.getNameAndTypeIndex());
        String methodName = ((ConstantInfo.Utf8)getConstant(nameAndType.getNameIndex())).getString();
        MethodInfo.Descriptor methodType = DescriptorParser.parseMethod(
                ((ConstantInfo.Utf8)getConstant(nameAndType.getDescriptorIndex())).getString());

        Value[] args = new Value[methodType.getParameters().size() + 1]; // including receiver
        for (int i = 0; i < args.length; i++) {
            args[args.length - 1 - i] = stack.pop();
        }
        vm.callMethod(methodName, methodType, args);
    }

    private ConstantInfo getConstant(int index) {
        return klass.getClassFile().getConstantPool()[index - 1];
    }
}
