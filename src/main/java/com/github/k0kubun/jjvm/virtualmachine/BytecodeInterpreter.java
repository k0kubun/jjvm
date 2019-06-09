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
import com.github.k0kubun.jjvm.classfile.MethodInfo.Descriptor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;
import java.util.stream.Stream;

// The core of the VirtualMachine.
// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
public class BytecodeInterpreter {
    private final VirtualMachine vm;
    private final Value.Class thisClass;
    private int pc; // program counter
    private final Deque<Value> stack;

    public BytecodeInterpreter(VirtualMachine vm, Value.Class klass) {
        this.vm = vm;
        this.thisClass = klass;
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
                case Iconst_M1:
                    stack.push(new Value(new FieldType.Int(), -1));
                    break;
                // case Iconst_0:
                case Iconst_1:
                    stack.push(new Value(new FieldType.Int(), 1));
                    break;
                case Iconst_2:
                    stack.push(new Value(new FieldType.Int(), 2));
                    break;
                case Iconst_3:
                    stack.push(new Value(new FieldType.Int(), 3));
                    break;
                case Iconst_4:
                    stack.push(new Value(new FieldType.Int(), 4));
                    break;
                case Iconst_5:
                    stack.push(new Value(new FieldType.Int(), 5));
                    break;
                // case Lconst_0:
                // case Lconst_1:
                // case Fconst_0:
                // case Fconst_1:
                // case Fconst_2:
                // case Dconst_0:
                // case Dconst_1:
                case Bipush:
                    stack.push(new Value(new FieldType.Int(), (int)instruction.getOperands()[0]));
                    break;
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
                // case Ldc_W:
                // case Ldc2_W:
                // case Iload:
                // case Lload:
                // case Fload:
                // case Dload:
                // case Aload:
                // case Iload_0:
                // case Iload_3:
                // case Lload_1:
                // case Fload_1:
                // case Dload_1:
                // case Dload_2:
                case Aload_0:
                case Iload_0:
                    stack.push(locals[0]);
                    break;
                case Aload_1:
                case Iload_1:
                    stack.push(locals[1]);
                    break;
                case Iload_2:
                    stack.push(locals[2]);
                    break;
                // case Aload_2:
                // case Aload_3:
                // case Iaload:
                // case Laload:
                // case Faload:
                // case Daload:
                // case Aaload:
                // case Baload:
                // case Caload:
                // case Saload:
                // case Istore:
                // case Lstore:
                // case Fstore:
                // case Dstore:
                // case Astore:
                // case Istore_3:
                // case Astore_0:
                case Istore_0:
                    locals[0] = stack.pop();
                    break;
                case Astore_1:
                case Istore_1:
                    locals[1] = stack.pop();
                    break;
                case Astore_2:
                case Istore_2:
                    locals[2] = stack.pop();
                    break;
                // case Astore_3:
                // case Iastore:
                // case Lastore:
                // case Fastore:
                // case Dastore:
                // case Aastore:
                // case Bastore:
                // case Castore:
                // case Sastore:
                // case Pop:
                // case Pop2:
                case Dup:
                    stack.push(stack.getFirst());
                    break;
                // case Dup_X1:
                // case Dup_X2:
                // case Dup2:
                // case Dup2_X1:
                // case Dup2_X2:
                // case Swap:
                case Iadd:
                    int[] ints = popInts(2);
                    stack.push(new Value(new FieldType.Int(), ints[0] + ints[1]));
                    break;
                // case Ladd:
                // case Fadd:
                // case Dadd:
                case Isub:
                    ints = popInts(2);
                    stack.push(new Value(new FieldType.Int(), ints[0] - ints[1]));
                    break;
                // case Lsub:
                // case Fsub:
                // case Dsub:
                case Imul:
                    ints = popInts(2);
                    stack.push(new Value(new FieldType.Int(), ints[0] * ints[1]));
                    break;
                // case Lmul:
                // case Fmul:
                // case Dmul:
                case Idiv:
                    ints = popInts(2);
                    stack.push(new Value(new FieldType.Int(), ints[0] / ints[1]));
                    break;
                // case Ldiv:
                // case Fdiv:
                // case Ddiv:
                case Irem:
                    ints = popInts(2);
                    stack.push(new Value(new FieldType.Int(), ints[0] % ints[1]));
                    break;
                // case Lrem:
                // case Frem:
                // case Drem:
                case Ineg:
                    stack.push(new Value(new FieldType.Int(), -((int)stack.pop().getValue())));
                    break;
                // case Lneg:
                // case Fneg:
                // case Dneg:
                // case Ishl:
                // case Lshl:
                // case Ishr:
                // case Lshr:
                // case Iushr:
                // case Lushr:
                // case Iand:
                // case Land:
                // case Ior:
                // case Lor:
                // case Ixor:
                // case Lxor:
                // case Iinc:
                // case Lcmp:
                // case Fcmpl:
                // case Fcmpg:
                // case Dcmpl:
                // case Dcmpg:
                // case Ifeq:
                // case Ifne:
                // case Iflt:
                // case Ifge:
                // case Ifgt:
                // case Ifle:
                // case IfIcmpeq:
                // case IfIcmpeq:
                // case IfIcmplt:
                // case IfIcmpge:
                // case IfIcmpgt:
                // case IfIcmple:
                // case IfAcmpeq:
                // case IfAcmpne:
                // case Goto:
                // case Jsr:
                // case Ret:
                // case Tableswitch:
                // case Lookupswitch:
                // case Ireturn:
                // case Lreturn:
                // case Freturn:
                // case Dreturn:
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
                    String methodName = getMethodName(instruction.getIndex());
                    Descriptor methodType = getMethodType(instruction.getIndex());
                    Value[] args = popStack(methodType.getParameters().size() + 1); // including receiver

                    if (args[0].getType().getType().equals("java.io.PrintStream") && methodName.equals("println") && args.length == 2) {
                        // Stub PrintStream#println implementation for now
                        System.out.println(args[1].getValue());
                    } else {
                        vm.callMethod(methodName, methodType, args);
                    }
                    break;
                case Invokespecial:
                    // TODO: handle `protected` specially
                    methodName = getMethodName(instruction.getIndex());
                    methodType = getMethodType(instruction.getIndex());
                    args = popStack(methodType.getParameters().size() + 1); // including receiver
                    vm.callMethod(methodName, methodType, args);
                    break;
                case Invokestatic:
                    methodName = getMethodName(instruction.getIndex());
                    methodType = getMethodType(instruction.getIndex());
                    args = popStack(methodType.getParameters().size());
                    vm.callStaticMethod(thisClass, methodName, methodType, args);
                    break;
                // case Invokeinterface:
                // case Invokedynamic:
                // case New:
                // case Newarray:
                // case Anewarray:
                // case Arraylength:
                // case Athrow:
                // case Checkcast:
                // case Instanceof:
                case Monitorenter:
                    stack.pop(); // TODO: synchronize this
                    break;
                // case Monitorexit:
                // case Wide:
                // case Multianewarray:
                // case Ifnull:
                case Ifnonnull:
                    if (stack.pop().getValue() != null) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                // case Goto_W:
                // case Jsr_W:
                default:
                    throw new RuntimeException("BytecodeInterpreter#execute does not implement opcode: " + opcode.getName());
            }

            pc++;
        }
    }

    private int[] popInts(int size) {
        return Arrays.stream(popStack(size)).mapToInt(v -> (int)v.getValue()).toArray();
    }

    private Value[] popStack(int size) {
        Value[] values = new Value[size];
        for (int i = 0; i < values.length; i++) {
            values[values.length - 1 - i] = stack.pop();
        }
        return values;
    }

    private String getMethodName(int methodIndex) {
        Methodref methodref = getConstant(methodIndex);
        NameAndType nameAndType = getConstant(methodref.getNameAndTypeIndex());
        return getName(nameAndType);
    }

    private Descriptor getMethodType(int methodIndex) {
        Methodref methodref = getConstant(methodIndex);
        NameAndType nameAndType = getConstant(methodref.getNameAndTypeIndex());
        return DescriptorParser.parseMethod(
                ((Utf8)getConstant(nameAndType.getDescriptorIndex())).getString());
    }

    private String getName(ConstantInfo.NamedInfo constant) {
        return ((ConstantInfo.Utf8)getConstant(constant.getNameIndex())).getString();
    }

    @SuppressWarnings("unchecked")
    private <T extends ConstantInfo> T getConstant(int index) {
        return (T)thisClass.getClassFile().getConstantPool()[index - 1];
    }
}
