package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFileParser.DescriptorParser;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.ConstantInfo.Fieldref;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.Instruction.Opcode;
import com.github.k0kubun.jjvm.classfile.Instruction;
import com.github.k0kubun.jjvm.classfile.MethodInfo;
import com.github.k0kubun.jjvm.classfile.MethodInfo.Descriptor;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

// The core of the VirtualMachine.
// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
public class BytecodeInterpreter {
    private final VirtualMachine vm;
    private final Value.Class thisClass;
    private final Deque<Value> stack;

    public BytecodeInterpreter(VirtualMachine vm, Value.Class klass) {
        this.vm = vm;
        this.thisClass = klass;
        this.stack = new ArrayDeque<>();
    }

    public Value execute(AttributeInfo.Code code, Value[] methodArgs, MethodInfo.ReturnDescriptor returnType) {
        Value[] locals = new Value[code.getMaxLocals()];
        for (int i = 0; i < methodArgs.length; i++) {
            locals[i] = methodArgs[i];
        }
        Instruction[] instructions = code.getInstructions();
        int pc = 0; // program counter

        while (true) {
            Instruction instruction = instructions[pc];
            Opcode opcode = instruction.getOpcode();

            switch (opcode) {
                // case Nop:
                case Aconst_Null:
                    stack.push(Value.Null());
                    break;
                case Iconst_M1:
                    stack.push(new Value(FieldType.INT, -1));
                    break;
                case Iconst_0:
                    stack.push(new Value(FieldType.INT, 0));
                    break;
                case Iconst_1:
                    stack.push(new Value(FieldType.INT, 1));
                    break;
                case Iconst_2:
                    stack.push(new Value(FieldType.INT, 2));
                    break;
                case Iconst_3:
                    stack.push(new Value(FieldType.INT, 3));
                    break;
                case Iconst_4:
                    stack.push(new Value(FieldType.INT, 4));
                    break;
                case Iconst_5:
                    stack.push(new Value(FieldType.INT, 5));
                    break;
                case Lconst_0:
                    stack.push(new Value(FieldType.LONG, 0L));
                    break;
                case Lconst_1:
                    stack.push(new Value(FieldType.LONG, 1L));
                    break;
                case Fconst_0:
                    stack.push(new Value(FieldType.FLOAT, 0F));
                    break;
                case Fconst_1:
                    stack.push(new Value(FieldType.FLOAT, 1F));
                    break;
                case Fconst_2:
                    stack.push(new Value(FieldType.FLOAT, 2F));
                    break;
                case Dconst_0:
                    stack.push(new Value(FieldType.DOUBLE, 0D));
                    break;
                case Dconst_1:
                    stack.push(new Value(FieldType.DOUBLE, 1D));
                    break;
                case Bipush:
                    stack.push(new Value(FieldType.INT, (int)instruction.getOperands()[0]));
                    break;
                case Sipush:
                    stack.push(new Value(FieldType.SHORT, (short)instruction.getIndex()));
                    break;
                case Ldc:
                case Ldc_W:
                    ConstantInfo constValue = getConstant(opcode == Opcode.Ldc ? instruction.getByte() : instruction.getIndex());
                    if (constValue instanceof ConstantInfo.String) {
                        FieldType type = DescriptorParser.parseField("Ljava/lang/String;");
                        stack.push(new Value(type, new Value.Object(((ConstantInfo.String)constValue).getString())));
                    } else if (constValue instanceof ConstantInfo.Integer) {
                        stack.push(new Value(FieldType.INT, ((ConstantInfo.Integer)constValue).getValue()));
                    } else if (constValue instanceof ConstantInfo.Float) {
                        stack.push(new Value(FieldType.FLOAT, ((ConstantInfo.Float)constValue).getValue()));
                    } else if (constValue instanceof ConstantInfo.Class) {
                        FieldType type = DescriptorParser.parseField("Ljava/lang/Class;");
                        String name = ((ConstantInfo.Class)constValue).getName();
                        if (name.equals("[B")) { // broken path. FIXME FIXME FIXME
                            stack.push(Value.Null());
                        } else {
                            stack.push(new Value(type, vm.getClass(name)));
                        }
                    } else {
                        throw new RuntimeException("Unexpected ConstantType in ldc: " + constValue.getType());
                    }
                    break;
                case Ldc2_W:
                    constValue = getConstant(instruction.getIndex());
                    if (constValue instanceof ConstantInfo.Long) {
                        stack.push(new Value(FieldType.LONG, ((ConstantInfo.Long) constValue).getValue()));
                    } else if (constValue instanceof ConstantInfo.Double) {
                        stack.push(new Value(FieldType.DOUBLE, ((ConstantInfo.Double)constValue).getValue()));
                    } else {
                        throw new RuntimeException("Unexpected ConstantType in ldc2_w: " + constValue);
                    }
                    break;
                case Iload:
                case Lload:
                case Fload:
                case Dload:
                case Aload:
                    stack.push(locals[instruction.getByte()]);
                    break;
                case Iload_0:
                case Lload_0:
                case Fload_0:
                case Dload_0:
                case Aload_0:
                    stack.push(locals[0]);
                    break;
                case Iload_1:
                case Lload_1:
                case Fload_1:
                case Dload_1:
                case Aload_1:
                    stack.push(locals[1]);
                    break;
                case Iload_2:
                case Lload_2:
                case Fload_2:
                case Dload_2:
                case Aload_2:
                    stack.push(locals[2]);
                    break;
                case Iload_3:
                case Lload_3:
                case Fload_3:
                case Dload_3:
                case Aload_3:
                    stack.push(locals[3]);
                    break;
                case Iaload:
                    Value arg = stack.pop();
                    Value receiver = stack.pop();
                    stack.push(new Value(
                            (FieldType.Int)((FieldType.ArrayType)receiver.getType()).getComponentType(),
                            ((int[])receiver.getValue())[(Integer)arg.getValue()]));
                    break;
                // case Laload:
                // case Faload:
                // case Daload:
                case Aaload:
                    arg = stack.pop();
                    receiver = stack.pop();
                    stack.push(new Value(
                            ((FieldType.ArrayType)receiver.getType()).getComponentType(),
                            ((Value.Object[])receiver.getValue())[(Integer)arg.getValue()]));
                    break;
                // case Baload:
                case Caload:
                    arg = stack.pop();
                    receiver = stack.pop();
                    stack.push(new Value(
                            (FieldType.Char)((FieldType.ArrayType)receiver.getType()).getComponentType(),
                            ((char[])receiver.getValue())[(Integer)arg.getValue()]));
                    break;
                // case Saload:
                case Istore:
                case Lstore:
                case Fstore:
                case Dstore:
                case Astore:
                    locals[instruction.getByte()] = stack.pop();
                    break;
                case Istore_0:
                case Lstore_0:
                case Fstore_0:
                case Dstore_0:
                case Astore_0:
                    locals[0] = stack.pop();
                    break;
                case Istore_1:
                case Lstore_1:
                case Fstore_1:
                case Dstore_1:
                case Astore_1:
                    locals[1] = stack.pop();
                    break;
                case Istore_2:
                case Lstore_2:
                case Fstore_2:
                case Dstore_2:
                case Astore_2:
                    locals[2] = stack.pop();
                    break;
                case Istore_3:
                case Lstore_3:
                case Fstore_3:
                case Dstore_3:
                case Astore_3:
                    locals[3] = stack.pop();
                    break;
                case Iastore:
                    Value value = stack.pop();
                    arg = stack.pop();
                    receiver = stack.pop();
                    ((int[])receiver.getValue())[(Integer)arg.getValue()] = (Integer)value.getValue();
                    break;
                // case Lastore:
                // case Fastore:
                // case Dastore:
                case Aastore:
                    arg = stack.pop();
                    Value index = stack.pop();
                    receiver = stack.pop();
                    ((Object[]) receiver.getValue())[(Integer) index.getValue()] = arg.getValue();
                    break;
                // case Bastore:
                case Castore:
                    arg = stack.pop();
                    index = stack.pop();
                    receiver = stack.pop();
                    ((char[])receiver.getValue())[(Integer)index.getValue()] = (char)arg.getIntValue();
                    break;
                // case Sastore:
                case Pop:
                    stack.pop();
                    break;
                // case Pop2:
                case Dup:
                    stack.push(stack.getFirst());
                    break;
                case Dup_X1:
                    Value value1 = stack.pop();
                    Value value2 = stack.pop();
                    stack.push(value1);
                    stack.push(value2);
                    stack.push(value1);
                    break;
                // case Dup_X2:
                // case Dup2:
                // case Dup2_X1:
                // case Dup2_X2:
                // case Swap:
                case Iadd:
                    int[] ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] + ints[1]));
                    break;
                case Ladd:
                    long[] longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] + longs[1]));
                    break;
                case Fadd:
                    float[] floats = popFloats(2);
                    stack.push(new Value(FieldType.FLOAT, floats[0] + floats[1]));
                    break;
                case Dadd:
                    double[] doubles = popDoubles(2);
                    stack.push(new Value(FieldType.DOUBLE, doubles[0] + doubles[1]));
                    break;
                case Isub:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] - ints[1]));
                    break;
                case Lsub:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] - longs[1]));
                    break;
                case Fsub:
                    floats = popFloats(2);
                    stack.push(new Value(FieldType.FLOAT, floats[0] - floats[1]));
                    break;
                case Dsub:
                    doubles = popDoubles(2);
                    stack.push(new Value(FieldType.DOUBLE, doubles[0] - doubles[1]));
                    break;
                case Imul:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] * ints[1]));
                    break;
                case Lmul:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] * longs[1]));
                    break;
                case Fmul:
                    floats = popFloats(2);
                    stack.push(new Value(FieldType.FLOAT, floats[0] * floats[1]));
                    break;
                case Dmul:
                    doubles = popDoubles(2);
                    stack.push(new Value(FieldType.DOUBLE, doubles[0] * doubles[1]));
                    break;
                case Idiv:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] / ints[1]));
                    break;
                case Ldiv:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] / longs[1]));
                    break;
                case Fdiv:
                    floats = popFloats(2);
                    stack.push(new Value(FieldType.FLOAT, floats[0] / floats[1]));
                    break;
                case Ddiv:
                    doubles = popDoubles(2);
                    stack.push(new Value(FieldType.DOUBLE, doubles[0] / doubles[1]));
                    break;
                case Irem:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] % ints[1]));
                    break;
                case Lrem:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] % longs[1]));
                    break;
                case Frem:
                    floats = popFloats(2);
                    stack.push(new Value(FieldType.FLOAT, floats[0] % floats[1]));
                    break;
                case Drem:
                    doubles = popDoubles(2);
                    stack.push(new Value(FieldType.DOUBLE, doubles[0] % doubles[1]));
                    break;
                case Ineg:
                    stack.push(new Value(FieldType.INT, -((int)stack.pop().getValue())));
                    break;
                case Lneg:
                    stack.push(new Value(FieldType.LONG, -((long)stack.pop().getValue())));
                    break;
                case Fneg:
                    stack.push(new Value(FieldType.FLOAT, -((float)stack.pop().getValue())));
                    break;
                case Dneg:
                    stack.push(new Value(FieldType.DOUBLE, -((double)stack.pop().getValue())));
                    break;
                case Ishl:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] << ints[1]));
                    break;
                case Lshl:
                    int intv = (Integer)stack.pop().getValue();
                    long longv = (Long)stack.pop().getValue();
                    stack.push(new Value(FieldType.LONG, longv << intv));
                    break;
                case Ishr:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] >> ints[1]));
                    break;
                case Lshr:
                    intv = (Integer)stack.pop().getValue();
                    longv = (Long)stack.pop().getValue();
                    stack.push(new Value(FieldType.LONG, longv >> intv));
                    break;
                case Iushr:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] >>> ints[1]));
                    break;
                case Lushr:
                    intv = (Integer)stack.pop().getValue();
                    longv = (Long)stack.pop().getValue();
                    stack.push(new Value(FieldType.LONG, longv >>> intv));
                    break;
                case Iand:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] & ints[1]));
                    break;
                case Land:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] & longs[1]));
                    break;
                case Ior:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] | ints[1]));
                    break;
                case Lor:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] | longs[1]));
                    break;
                case Ixor:
                    ints = popInts(2);
                    stack.push(new Value(FieldType.INT, ints[0] ^ ints[1]));
                    break;
                case Lxor:
                    longs = popLongs(2);
                    stack.push(new Value(FieldType.LONG, longs[0] ^ longs[1]));
                    break;
                case Iinc:
                    intv = (Integer)locals[instruction.getOperands()[0]].getValue();
                    locals[instruction.getOperands()[0]] =
                            new Value(FieldType.INT, intv + instruction.getOperands()[1]);
                    break;
                case I2l:
                    intv = (Integer)stack.pop().getValue();
                    stack.push(new Value(FieldType.LONG, (long)intv));
                    break;
                case I2f:
                    intv = (Integer)stack.pop().getValue();
                    stack.push(new Value(FieldType.FLOAT, (float)intv));
                    break;
                // case I2d:
                case L2i:
                    longv = (Long)stack.pop().getValue();
                    stack.push(new Value(FieldType.INT, (int)longv));
                    break;
                // case L2f:
                // case L2d:
                case F2i:
                    float floatv = (Float)stack.pop().getValue();
                    stack.push(new Value(FieldType.INT, (int)floatv));
                    break;
                // case F2l:
                // case F2d:
                // case D2i:
                // case D2l:
                // case D2f:
                // case I2b:
                // case I2c:
                // case I2s:
                case Lcmp:
                    // TODO: test this instruction
                    longs = popLongs(2);
                    if (longs[0] < longs[1]) {
                        stack.push(new Value(FieldType.INT, 1));
                    } else if (longs[0] == longs[1]) {
                        stack.push(new Value(FieldType.INT, 0));
                    } else {
                        stack.push(new Value(FieldType.INT, -1));
                    }
                    break;
                case Fcmpl:
                    // TODO: test this instruction
                    floats = popFloats(2);
                    if (floats[0] < floats[1]) {
                        stack.push(new Value(FieldType.INT, 1));
                    } else if (floats[0] == floats[1]) {
                        stack.push(new Value(FieldType.INT, 0));
                    } else {
                        stack.push(new Value(FieldType.INT, -1));
                    }
                    break;
                case Fcmpg:
                    // TODO: test this instruction
                    floats = popFloats(2);
                    if (floats[0] == floats[1]) {
                        stack.push(new Value(FieldType.INT, 0));
                    } else if (floats[0] < floats[1]) {
                        stack.push(new Value(FieldType.INT, -1));
                    } else {
                        stack.push(new Value(FieldType.INT, 1));
                    }
                    break;
                // case Dcmpl:
                // case Dcmpg:
                case Ifeq:
                    intv = stack.pop().getIntValue();
                    if (intv == 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Ifne:
                    intv = stack.pop().getIntValue();
                    if (intv != 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Iflt:
                    intv = (int)stack.pop().getValue();
                    if (intv < 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Ifge:
                    intv = (int)stack.pop().getValue();
                    if (intv >= 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Ifgt:
                    intv = (int)stack.pop().getValue();
                    if (intv > 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Ifle:
                    intv = (int)stack.pop().getValue();
                    if (intv <= 0) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmpeq:
                    ints = popInts(2);
                    if (ints[0] == ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmpne:
                    ints = popInts(2);
                    if (ints[0] != ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmplt:
                    ints = popInts(2);
                    if (ints[0] < ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmpge:
                    ints = popInts(2);
                    if (ints[0] >= ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmpgt:
                    ints = popInts(2);
                    if (ints[0] > ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfIcmple:
                    ints = popInts(2);
                    if (ints[0] <= ints[1]) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfAcmpeq:
                    Value[] values = popStack(2);
                    if (values[0].getValue() == values[1].getValue()) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case IfAcmpne:
                    values = popStack(2);
                    if (values[0].getValue() != values[1].getValue()) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
                case Goto:
                    pc += instruction.getIndex();
                    continue;
                // case Jsr:
                // case Ret:
                case Tableswitch:
                    intv = (int)stack.pop().getValue();
                    int low = instruction.getIntArg(1);
                    int high = instruction.getIntArg(2);
                    if (low <= intv && intv <= high) {
                        pc += instruction.getIntArg(3 + (intv - low));
                    } else {
                        pc += instruction.getIntArg(0);
                    }
                    continue;
                // case Lookupswitch:
                case Ireturn:
                    value = stack.pop();
                    intv = value.getIntValue();
                    if (returnType instanceof FieldType.Int) {
                        return value;
                    } else if (returnType instanceof FieldType.Char) {
                        return new Value(FieldType.CHAR, (char)intv);
                    } else if (returnType instanceof FieldType.Boolean) {
                        return new Value(FieldType.BOOLEAN, intv == 1);
                    } else {
                        throw new RuntimeException("unexpected returnType in ireturn: " + returnType);
                    }
                case Lreturn:
                case Freturn:
                case Dreturn:
                case Areturn:
                    return stack.pop();
                case Return:
                    return null;
                case Getstatic:
                    Fieldref field = getFieldConstant(instruction.getIndex());
                    Value.Class klass = vm.getClass(field.getClassInfo().getName());
                    String name = field.getNameAndType().getName();
                    stack.push(klass.getField(name)); // XXX: do we need to check type here?
                    break;
                case Putstatic:
                    field = getFieldConstant(instruction.getIndex());
                    klass = vm.getClass(field.getClassInfo().getName());
                    klass.setField(field.getNameAndType().getName(), stack.pop());
                    break;
                case Getfield:
                    field = getFieldConstant(instruction.getIndex());
                    Value.Object object = (Value.Object)stack.pop().getValue();
                    stack.push(object.getField(field.getNameAndType().getName()));
                    break;
                case Putfield:
                    field = getFieldConstant(instruction.getIndex());
                    arg = stack.pop();
                    receiver = stack.pop();
                    ((Value.Object)receiver.getValue()).setField(field.getNameAndType().getName(), arg);
                    break;
                case Invokevirtual:
                    String methodName = getMethodConstant(instruction.getIndex()).getNameAndType().getName();
                    Descriptor methodType = getMethodConstant(instruction.getIndex()).getNameAndType().getMethodDescriptor();
                    Value[] args = popStack(methodType.getParameters().size() + 1); // including receiver
                    pushIfNotNull(vm.callMethod(methodName, methodType, args));
                    break;
                case Invokespecial:
                    String methodClassName = getMethodConstant(instruction.getIndex()).getClassInfo().getName();
                    methodName = getMethodConstant(instruction.getIndex()).getNameAndType().getName();
                    methodType = getMethodConstant(instruction.getIndex()).getNameAndType().getMethodDescriptor();
                    args = popStack(methodType.getParameters().size() + 1); // including receiver
                    pushIfNotNull(vm.callMethodSpecial(methodClassName, methodName, methodType, args));
                    break;
                case Invokestatic:
                    methodClassName = getMethodConstant(instruction.getIndex()).getClassInfo().getName();
                    methodName = getMethodConstant(instruction.getIndex()).getNameAndType().getName();
                    methodType = getMethodConstant(instruction.getIndex()).getNameAndType().getMethodDescriptor();
                    args = popStack(methodType.getParameters().size());
                    pushIfNotNull(vm.callStaticMethod(methodClassName, methodName, methodType, args));
                    break;
                case Invokeinterface:
                    methodName = getInterfaceMethodConstant(instruction.getIndex()).getNameAndType().getName();
                    methodType = getInterfaceMethodConstant(instruction.getIndex()).getNameAndType().getMethodDescriptor();
                    args = popStack(methodType.getParameters().size() + 1); // including receiver
                    pushIfNotNull(vm.callMethod(methodName, methodType, args));
                    break;
                // case Invokedynamic:
                case New:
                    String className = getClassConstant(instruction.getIndex()).getName();
                    FieldType type = DescriptorParser.parseField(String.format("L%s;", className));
                    object = new Value.Object();
                    vm.initializeObject(object, className);
                    stack.push(new Value(type, object));
                    break;
                case Newarray:
                    int size = (Integer)stack.pop().getValue();
                    switch (instruction.getOperands()[0]) {
                        case 4: // T_BOOLEAN
                            stack.push(new Value(new FieldType.ArrayType(FieldType.BOOLEAN), new boolean[size]));
                            break;
                        case 5: // T_CHAR
                            stack.push(new Value(new FieldType.ArrayType(FieldType.CHAR), new char[size]));
                            break;
                        case 6: // T_FLOAT
                            stack.push(new Value(new FieldType.ArrayType(FieldType.FLOAT), new float[size]));
                            break;
                        case 7: // T_DOUBLE
                            stack.push(new Value(new FieldType.ArrayType(FieldType.DOUBLE), new double[size]));
                            break;
                        case 8: // T_BYTE
                            stack.push(new Value(new FieldType.ArrayType(FieldType.BYTE), new byte[size]));
                            break;
                        case 9: // T_SHORT
                            stack.push(new Value(new FieldType.ArrayType(FieldType.SHORT), new short[size]));
                            break;
                        case 10: // T_INT
                            stack.push(new Value(new FieldType.ArrayType(FieldType.INT), new int[size]));
                            break;
                        case 11: // T_LONG
                            stack.push(new Value(new FieldType.ArrayType(FieldType.LONG), new long[size]));
                            break;
                        default:
                            throw new RuntimeException(String.format("unexpected tag is given with newarray: %d", instruction.getOperands()[0]));
                    }
                    break;
                case Anewarray:
                    arg = stack.pop();
                    className = getClassConstant(instruction.getIndex()).getName();
                    stack.push(new Value(
                            new FieldType.ArrayType(new FieldType.ObjectType(className)),
                            new Value.Object[(Integer)arg.getValue()]));
                    break;
                case Arraylength:
                    receiver = stack.pop();
                    stack.push(new Value(FieldType.INT, getArrayLength(receiver)));
                    break;
                // case Athrow:
                case Checkcast:
                    constValue = getConstant(instruction.getIndex());
                    if (constValue instanceof ConstantInfo.Class) {
                        receiver = stack.pop();
                        className = ((ConstantInfo.Class)constValue).getName();
                        if (receiver.getType().getType().equals(className.replace('/', '.'))) {
                            stack.push(receiver);
                        } else {
                            stack.push(receiver);
                            // stub. FIXME FIXME FIXME
                            //throw new RuntimeException("This path of checkcast is not implemented yet");
                        }
                    } else {
                        throw new RuntimeException("unexpected type of ConstantInfo in instanceof: " + constValue);
                    }
                    break;
                case Instanceof:
                    constValue = getConstant(instruction.getIndex());
                    if (constValue instanceof ConstantInfo.Class) {
                        receiver = stack.pop();
                        className = ((ConstantInfo.Class)constValue).getName();
                        if (receiver.getType().getType().equals(className.replace('/', '.'))) {
                            stack.push(new Value(FieldType.INT, 1));
                        } else {
                            throw new RuntimeException("This path of instanceof is not implemented yet");
                        }
                    } else {
                        throw new RuntimeException("unexpected type of ConstantInfo in instanceof: " + constValue);
                    }
                    break;
                case Monitorenter:
                case Monitorexit:
                    stack.pop(); // TODO: synchronize this
                    break;
                // case Wide:
                // case Multianewarray:
                case Ifnull:
                    if (stack.pop().getValue() == null) {
                        pc += instruction.getIndex();
                        continue;
                    }
                    break;
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

            pc += 1 + instruction.getOperands().length;
        }
    }

    // XXX: do we need to verify return type is void?
    private void pushIfNotNull(Value val) {
        if (val != null) {
            stack.push(val);
        }
    }

    private int[] popInts(int size) {
        return Arrays.stream(popStack(size)).mapToInt(v -> v.getIntValue()).toArray();
    }

    private long[] popLongs(int size) {
        return Arrays.stream(popStack(size)).mapToLong(v -> (long)v.getValue()).toArray();
    }

    private float[] popFloats(int size) {
        float[] floats = new float[size];
        Value[] values = popStack(size);
        for (int i = 0; i < size; i++) {
            floats[i] = (float)values[i].getValue();
        }
        return floats;
    }

    private double[] popDoubles(int size) {
        return Arrays.stream(popStack(size)).mapToDouble(v -> (double)v.getValue()).toArray();
    }

    private Value[] popStack(int size) {
        Value[] values = new Value[size];
        for (int i = 0; i < values.length; i++) {
            values[values.length - 1 - i] = stack.pop();
        }
        return values;
    }

    private int getArrayLength(Value value) {
        FieldType type = ((FieldType.ArrayType)value.getType()).getComponentType();
        if (type instanceof FieldType.ObjectType) {
            return ((Object[]) value.getValue()).length;
        } else if (type instanceof FieldType.Int) {
            return ((int[])value.getValue()).length;
        } else if (type instanceof FieldType.Char) {
            return ((char[])value.getValue()).length;
        } else {
            throw new RuntimeException("unexpected array type for arraylength: " + type.toString());
        }
    }

    private ConstantInfo.Class getClassConstant(int index) {
        return (ConstantInfo.Class)getConstant(index);
    }

    private ConstantInfo.Fieldref getFieldConstant(int index) {
        return (ConstantInfo.Fieldref)getConstant(index);
    }

    private ConstantInfo.Methodref getMethodConstant(int index) {
        return (ConstantInfo.Methodref)getConstant(index);
    }

    private ConstantInfo.InterfaceMethodref getInterfaceMethodConstant(int index) {
        return (ConstantInfo.InterfaceMethodref)getConstant(index);
    }

    private ConstantInfo getConstant(int index) {
        return thisClass.getClassFile().getConstantPool()[index - 1];
    }
}
