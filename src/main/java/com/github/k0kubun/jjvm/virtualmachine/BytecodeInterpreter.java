package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFileParser.DescriptorParser;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.ConstantInfo.Fieldref;
import com.github.k0kubun.jjvm.classfile.ConstantInfo.Methodref;
import com.github.k0kubun.jjvm.classfile.ConstantInfo.NameAndType;
import com.github.k0kubun.jjvm.classfile.ConstantInfo.Utf8;
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
                case Iconst_1:
                    stack.push(new Value(new FieldType.Int(), 1));
                    break;
                case Iconst_2:
                    stack.push(new Value(new FieldType.Int(), 2));
                    break;
                // case Iconst_3:
                // case Bipush:
                // case Sipush:
                case Ldc:
                    ConstantInfo constValue = getConstant(instruction.getOperands()[0]);
                    if (constValue instanceof ConstantInfo.String) {
                        FieldType type = DescriptorParser.parseField("Ljava/lang/String;");
                        stack.push(new Value(type,
                                ((Utf8)getConstant(((ConstantInfo.String) constValue).getNameIndex())).getString()));
                    } else {
                        throw new RuntimeException("Unexpected ConstantType in ldc: " + constValue.getType());
                    }
                    break;
                // case Aload:
                // case Iload_0:
                // case Iload_3:
                // case Lload_1:
                // case Fload_1:
                // case Dload_1:
                // case Dload_2:
                case Aload_0:
                    stack.push(locals[0]);
                    break;
                case Iload_1:
                case Aload_1:
                    stack.push(locals[1]);
                    break;
                case Iload_2:
                    stack.push(locals[2]);
                    break;
                // case Aload_2:
                // case Aload_3:
                // case Caload:
                // case Istore_3:
                // case Astore_0:
                case Istore_1:
                case Astore_1:
                    locals[1] = stack.pop();
                    break;
                case Istore_2:
                case Astore_2:
                    locals[2] = stack.pop();
                    break;
                // case Astore_3:
                // case Astore:
                // case Pop:
                case Dup:
                    stack.push(stack.getFirst());
                    break;
                case Iadd:
                    int right = (int)stack.pop().getValue();
                    int left = (int)stack.pop().getValue();
                    stack.push(new Value(new FieldType.Int(), right + left));
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
                    Fieldref field = getConstant(instruction.getIndex());
                    NameAndType nameAndType = getConstant(field.getNameAndTypeIndex());
                    Value.Class klass = vm.getClass(getName(getConstant(field.getClassIndex())));
                    stack.push(klass.getField(getName(nameAndType))); // XXX: do we need to check type here?
                    break;
                // case Putstatic:
                // case Getfield:
                // case Putfield:
                case Invokevirtual:
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
        Methodref methodref = getConstant(methodIndex);
        NameAndType nameAndType = getConstant(methodref.getNameAndTypeIndex());
        String methodName = getName(nameAndType);
        MethodInfo.Descriptor methodType = DescriptorParser.parseMethod(
                ((Utf8)getConstant(nameAndType.getDescriptorIndex())).getString());

        Value[] args = new Value[methodType.getParameters().size() + 1]; // including receiver
        for (int i = 0; i < args.length; i++) {
            args[args.length - 1 - i] = stack.pop();
        }

        if (args[0].getType().getType().equals("java.io.PrintStream") && methodName.equals("println") && args.length == 2) {
            // Stub PrintStream#println implementation for now
            System.out.println(args[1].getValue());
        } else {
            vm.callMethod(methodName, methodType, args);
        }
    }

    private String getName(ConstantInfo.NamedInfo constant) {
        return ((ConstantInfo.Utf8)getConstant(constant.getNameIndex())).getString();
    }

    @SuppressWarnings("unchecked")
    private <T extends ConstantInfo> T getConstant(int index) {
        return (T)klass.getClassFile().getConstantPool()[index - 1];
    }
}
