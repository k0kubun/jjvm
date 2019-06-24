package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;
import com.github.k0kubun.jjvm.classfile.FieldInfo;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

import java.util.HashMap;
import java.util.Map;

// A class hodling VM states and providing VM-related interfaces.
public class VirtualMachine {
    private final Map<String, Value.Class> classMap;
    private final ClassLoader classLoader;

    // Threads::create_vm() equivalent
    public VirtualMachine(String classPath) {
        classMap = new HashMap<>();
        classLoader = new ClassLoader(classPath);

        // initializeClass("java/lang/String");
        initializeClass("java/lang/System");

        callInitializeSystemClass();

        // TODO: implement field initializer
        Value.Class runtime = initializeClass("java/lang/Runtime");
        runtime.setField("currentRuntime", new Value(fieldType("Ljava/lang/Runtime;"), new Value.Object()));
        Value.Class shutdown = initializeClass("java/lang/Shutdown");
        shutdown.setField("lock", new Value(fieldType("Ljava/lang/Shutdown$Lock;"), new Value.Object()));
        shutdown.setField("haltLock", new Value(fieldType("Ljava/lang/Shutdown$Lock;"), new Value.Object()));
        shutdown.setField("hooks", new Value(new FieldType.ArrayType(fieldType("Ljava/lang/Runnable;")), new Value.Object[10]));
    }

    // Call an instance method
    public Value callMethod(String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(args[0].getType());
        MethodInfo method = searchMethod(klass, methodName, methodType);
        return executeMethod(klass, method, args);
    }

    // Call an instance method, but specialized for invokespecial
    public Value callMethodSpecial(String methodClassName, String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(methodClassName);
        MethodInfo method;
        try {
            method = searchMethod(klass, methodName, methodType);
        } catch (NoMethodException e) {
            if (methodType.getReturn() instanceof MethodInfo.VoidDescriptor
                    && methodType.getParameters().size() == 0 && methodName.equals("<init>")) {
                // ignore undefined <init>:()V call
                return null;
            } else {
                throw e;
            }
        }
        // TODO: handle `protected` specially
        return executeMethod(klass, method, args);
    }

    public Value callStaticMethod(String methodClassName, String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(methodClassName);
        MethodInfo method = searchMethod(klass, methodName, methodType);
        return executeMethod(klass, method, args);
    }

    public Value.Class getClass(String name) {
        return getClass(fieldType(String.format("L%s;", name)));
    }

    Value defaultValueOfType(FieldType fieldType) {
        // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.3
        if (fieldType instanceof FieldType.Int) {
            return new Value(new FieldType.Int(), 0);
        } else if (fieldType instanceof FieldType.Long) {
            return new Value(new FieldType.Long(), 0);
        } else if (fieldType instanceof FieldType.Double) {
            return new Value(new FieldType.Double(), +0.0);
        } else if (fieldType instanceof FieldType.Boolean) {
            return new Value(new FieldType.Boolean(), false);
        } else if (fieldType instanceof FieldType.ArrayType || fieldType instanceof FieldType.ObjectType) {
            return Value.Null();
        } else {
            throw new RuntimeException("unexpected field type in new: " + fieldType);
        }
    }

    private Value.Class initializeClass(String klass) {
        ClassFile classFile = classLoader.loadClass(klass);
        Value.Class value = new Value.Class(classFile);

        for (FieldInfo field : classFile.getFields()) {
            if (!field.getAccessFlags().contains(FieldInfo.AccessFlag.ACC_STATIC))
                continue;
            FieldType fieldType = field.getDescriptor();
            value.setField(field.getName(), defaultValueOfType(fieldType));
        }

        classMap.put(classFile.getThisClassName(), value);
        return value;
    }

    // Get or load a class from FieldType
    private Value.Class getClass(FieldType type) {
        if (type instanceof FieldType.ObjectType) {
            FieldType.ObjectType objectType = (FieldType.ObjectType)type;
            if (classMap.containsKey(objectType.getClassName())) {
                return classMap.get(objectType.getClassName());
            } else {
                Value.Class klass = initializeClass(objectType.getClassName());
                classMap.put(objectType.getClassName(), klass);
                return klass;
            }
        } else {
            throw new RuntimeException("unexpected FieldType is given to getClass: " + type.getType());
        }
    }

    // `call_initializeSystemClass` equivalent
    private void callInitializeSystemClass() {
        Value.Class system = classMap.get("java/lang/System");
        system.setField("out", new Value(fieldType("Ljava/io/PrintStream;"), new Value.Object()));
        system.setField("err", new Value(fieldType("Ljava/io/PrintStream;"), new Value.Object()));
    }

    private FieldType fieldType(String type) {
        return ClassFileParser.DescriptorParser.parseField(type);
    }

    private MethodInfo searchMethod(Value.Class klass, String methodName, MethodInfo.Descriptor methodType) {
        for (MethodInfo method : klass.getClassFile().getMethods()) {
            if (method.getName().equals(methodName) && method.getDescriptor().equals(methodType)) {
                return method;
            }
        }
        throw new NoMethodException(String.format("%s.%s (%s)",
                klass.getClassFile().getThisClassName(), methodName, methodType.toString()));
    }

    private Value executeMethod(Value.Class klass, MethodInfo method, Value[] args) {
        //System.out.println(klass.getClassFile().getThisClassName() + "." + method.getName());
        if (method.getAccessFlags().contains(MethodInfo.AccessFlag.ACC_NATIVE)) {
            // TODO: Carve out this logic
            if (klass.getClassFile().getThisClassName().equals("java/lang/System") && method.getName().equals("arraycopy")) {
                System.arraycopy(args[0].getValue(), (Integer) args[1].getValue(),
                        args[2].getValue(), (Integer) args[3].getValue(), (Integer) args[4].getValue());
                return null;
            } else if (klass.getClassFile().getThisClassName().equals("java/lang/Shutdown") && method.getName().equals("halt0")) {
                System.exit((int)args[0].getValue());
                return null;
            } else {
                throw new RuntimeException("Unsupported native method: " + klass.getClassFile().getThisClassName() + "." + method.getName());
            }
        }
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        return new BytecodeInterpreter(this, klass).execute(code, args, method.getDescriptor().getReturn());
    }

    private static class NoMethodException extends RuntimeException {
        public NoMethodException(String message) {
            super(message);
        }
    }
}
