package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.FieldInfo;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

// A class hodling VM states and providing VM-related interfaces.
public class VirtualMachine {
    private final Map<String, Value.Class> classMap;
    private final ClassLoader classLoader;
    private final Set<String> clinitBlacklist;
    private int callDepth;
    private final boolean traceCall;

    // Threads::create_vm() equivalent
    public VirtualMachine(String classPath, boolean trace) {
        classMap = new HashMap<>();
        classLoader = new ClassLoader(classPath);

        callDepth = 0;
        traceCall = trace;

        clinitBlacklist = new HashSet<>();
        initializeClinitBlacklist();

        // initializeClass("java/lang/String");
        initializeClass("java/lang/System");
        initializeClass("java/lang/Class");

        callInitializeSystemClass();
    }

    // Call an instance method
    public Value callMethod(String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(args[0].getType());
        MethodSearchResult result = searchMethod(klass, methodName, methodType);
        return executeMethod(result.klass, result.method, args);
    }

    // Call an instance method, but specialized for invokespecial
    public Value callMethodSpecial(String methodClassName, String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(methodClassName);
        MethodSearchResult result;
        try {
            result = searchMethod(klass, methodName, methodType);
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
        return executeMethod(result.klass, result.method, args);
    }

    public Value callStaticMethod(String methodClassName, String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(methodClassName);
        MethodSearchResult result = searchMethod(klass, methodName, methodType);
        return executeMethod(result.klass, result.method, args);
    }

    public Value.Class getClass(String name) {
        return getClass(fieldType(String.format("L%s;", name)));
    }

    void initializeObject(Value.Object object, String className) {
        ClassFile classFile = this.getClass(className).getClassFile();
        if (classFile.getSuperClassName() != null) {
            initializeObject(object, classFile.getSuperClassName());
        }

        for (FieldInfo fieldInfo : classFile.getFields()) {
            if (fieldInfo.getAccessFlags().contains(FieldInfo.AccessFlag.ACC_STATIC))
                continue;

            FieldType fieldType = fieldInfo.getDescriptor();
            object.setField(fieldInfo.getName(), defaultValueOfType(fieldType));
        }
    }

    private Value defaultValueOfType(FieldType fieldType) {
        // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html#jvms-2.3
        if (fieldType instanceof FieldType.Int) {
            return new Value(FieldType.INT, 0);
        } else if (fieldType instanceof FieldType.Long) {
            return new Value(FieldType.LONG, 0L);
        } else if (fieldType instanceof FieldType.Char) {
            return new Value(FieldType.CHAR, '\u0000');
        } else if (fieldType instanceof FieldType.Float) {
            return new Value(FieldType.FLOAT, +0.0F);
        } else if (fieldType instanceof FieldType.Double) {
            return new Value(FieldType.DOUBLE, +0.0D);
        } else if (fieldType instanceof FieldType.Boolean) {
            return new Value(FieldType.BOOLEAN, false);
        } else if (fieldType instanceof FieldType.ArrayType || fieldType instanceof FieldType.ObjectType) {
            return Value.Null();
        } else {
            throw new RuntimeException("unexpected field type in new: " + fieldType);
        }
    }

    private Value getConstantValue(FieldType fieldType, ConstantInfo constantValue) {
        if (constantValue instanceof ConstantInfo.Long) {
            return new Value((FieldType.Long)fieldType, ((ConstantInfo.Long)constantValue).getValue());
        } else if (constantValue instanceof ConstantInfo.Float) {
            return new Value((FieldType.Float)fieldType, ((ConstantInfo.Float)constantValue).getValue());
        } else if (constantValue instanceof ConstantInfo.Double) {
            return new Value((FieldType.Double)fieldType, ((ConstantInfo.Double)constantValue).getValue());
        } else if (constantValue instanceof ConstantInfo.Integer) {
            if (fieldType instanceof FieldType.Int) {
                return new Value((FieldType.Int) fieldType, ((ConstantInfo.Integer) constantValue).getValue());
            // } else if (fieldType instanceof FieldType.Short) {
            //     return new Value((FieldType.Short) fieldType, ((ConstantInfo.Short) constantValue).getValue());
            // } else if (fieldType instanceof FieldType.Char) {
            //     return new Value((FieldType.Char) fieldType, ((ConstantInfo.Char) constantValue).getValue());
            // } else if (fieldType instanceof FieldType.Byte) {
            //     return new Value((FieldType.Byte) fieldType, ((ConstantInfo.Byte) constantValue).getValue());
            } else if (fieldType instanceof FieldType.Boolean) {
                return new Value((FieldType.Boolean) fieldType, ((ConstantInfo.Integer) constantValue).getValue() != 0);
            } else {
                throw new RuntimeException("unexpected FieldType in ConstantValue: " + fieldType);
            }
        } else if (constantValue instanceof ConstantInfo.String) {
            return new Value(fieldType, new Value.Object(((ConstantInfo.String)constantValue).getString()));
        } else {
            throw new RuntimeException("unexpected ConstantInfo in ConstantValue: " + constantValue);
        }
    }

    private Value.Class initializeClass(String klass) {
        ClassFile classFile = classLoader.loadClass(klass);
        Value.Class value = new Value.Class(classFile);

        for (FieldInfo field : classFile.getFields()) {
            if (!field.getAccessFlags().contains(FieldInfo.AccessFlag.ACC_STATIC))
                continue;

            FieldType fieldType = field.getDescriptor();
            if (field.getConstantValueAttribute() != null) {
                ConstantInfo constantValue = field.getConstantValueAttribute().getConstantValue();
                value.setField(field.getName(), getConstantValue(fieldType, constantValue));
            } else {
                value.setField(field.getName(), defaultValueOfType(fieldType));
            }
        }
        classMap.put(classFile.getThisClassName(), value);

        if (!clinitBlacklist.contains(classFile.getThisClassName())) { // <clinit> of these classes are buggy now
            MethodInfo.Descriptor clinitType = ClassFileParser.DescriptorParser.parseMethod("()V");
            try {
                MethodSearchResult result = searchMethod(value, "<clinit>", clinitType);
                String methodClass = result.klass.getClassFile().getThisClassName();
                if (methodClass.equals(klass) || !classMap.containsKey(methodClass)) { // avoid duplicated clinit by inheritance
                    executeMethod(result.klass, result.method, new Value[0]);
                }
            } catch (NoMethodException e) {
                // ignore undefined <clinit>:()V call
            }
        }
        return value;
    }

    // stub: <clinit> of these classes are buggy now
    private void initializeClinitBlacklist() {
        clinitBlacklist.add("java/lang/Exception");
        clinitBlacklist.add("java/lang/IllegalArgumentException");
        clinitBlacklist.add("java/lang/RuntimeException");
        clinitBlacklist.add("java/lang/Throwable");
        clinitBlacklist.add("java/nio/Bits");
        clinitBlacklist.add("java/util/concurrent/atomic/AtomicInteger");
        clinitBlacklist.add("java/util/concurrent/atomic/AtomicReferenceFieldUpdater$AtomicReferenceFieldUpdaterImpl");
        clinitBlacklist.add("sun/misc/SharedSecrets");
        clinitBlacklist.add("sun/misc/Unsafe");
        clinitBlacklist.add("sun/reflect/Reflection");
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
        MethodSearchResult result = searchMethod(system, "initializeSystemClass",
                ClassFileParser.DescriptorParser.parseMethod("()V"));
        executeMethod(result.klass, result.method, new Value[0]);
    }

    private FieldType fieldType(String type) {
        return ClassFileParser.DescriptorParser.parseField(type);
    }

    private MethodSearchResult searchMethod(Value.Class klass, String methodName, MethodInfo.Descriptor methodType) {
        for (MethodInfo method : klass.getClassFile().getMethods()) {
            if (method.getName().equals(methodName) && method.getDescriptor().equals(methodType)) {
                return new MethodSearchResult(klass, method);
            }
        }
        if (klass.getClassFile().getSuperClass() != null) {
            Value.Class superClass = getClass(klass.getClassFile().getSuperClass().getName());
            return searchMethod(superClass, methodName, methodType);
        }
        throw new NoMethodException(String.format("%s.%s (%s)",
                klass.getClassFile().getThisClassName(), methodName, methodType.toString()));
    }

    private Value executeMethod(Value.Class klass, MethodInfo method, Value[] args) {
        if (traceCall) {
            for (int i = 0; i < callDepth; i++)
                System.out.print("  ");
            System.out.println(klass.getClassFile().getThisClassName() + "." + method.getName());
        }

        if (method.getAccessFlags().contains(MethodInfo.AccessFlag.ACC_NATIVE)) {
            return NativeMethod.dispatch(klass, method, args);
        }

        MethodStubResult result = dispatchStubMethod(klass, method, args);
        if (result.isStub) {
            return result.value;
        }

        callDepth++;
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        Value ret = new BytecodeInterpreter(this, klass).execute(code, args, method.getDescriptor().getReturn());
        callDepth--;
        return ret;
    }

    // Temporary measures... FIXME: This method should go away
    private MethodStubResult dispatchStubMethod(Value.Class klass, MethodInfo method, Value[] args) {
        String className = klass.getClassFile().getThisClassName();
        Value ret = Value.Null();
        boolean isStub = true;

        // Stub properties until initProperties is implemented properly.
        if (className.equals("sun/misc/VM") && method.getName().equals("saveAndRemoveProperties")) {
            ret = null;
        }
        else if (className.equals("java/util/Properties") && method.getName().equals("getProperty")) {
            String property = String.valueOf((char[])((Value.Object)args[1].getValue()).getField("value").getValue());
            if (property.equals("sun.stdout.encoding") || property.equals("sun.stderr.encoding")) { // how can we get it properly?
                ret = new Value(new FieldType.ObjectType("java/lang/String"), new Value.Object("UTF-8"));
            } else {
                ret = new Value(new FieldType.ObjectType("java/lang/String"), new Value.Object(System.getProperty(property)));
            }
        }
        else if (className.equals("java/util/Properties") && method.getName().equals("setProperty")) {
            ret = args[1];
        }
        // Following methods are Not working...
        else if (className.equals("java/util/concurrent/atomic/AtomicReferenceFieldUpdater") && method.getName().equals("newUpdater")) {
            ret = new Value(new FieldType.ObjectType("java/util/concurrent/atomic/AtomicReferenceFieldUpdater"), new Value.Object());
        }
        else if (className.startsWith("sun/nio/cs/StandardCharsets$") && method.getName().equals("init")) {
            ret = null;
        }
        else if (className.equals("java/lang/ThreadLocal") && method.getName().equals("<init>")) {
            ret = null;
        }
        else if (className.equals("sun/nio/cs/FastCharsetProvider") && method.getName().equals("charsetForName")) {
            ret = new Value(new FieldType.ObjectType("java/nio/charset/Charset"), new Value.Object());
        }
        else if (className.equals("java/nio/charset/Charset") && method.getName().equals("forName")) {
            ret = new Value(new FieldType.ObjectType("java/nio/charset/Charset"), new Value.Object());
        }
        else if (className.equals("java/nio/charset/Charset") && method.getName().equals("newEncoder")) {
            ret = new Value(new FieldType.ObjectType("java/nio/charset/CharsetEncoder"), new Value.Object());
        }
        else if (className.equals("java/nio/charset/CharsetEncoder") && method.getName().equals("charset")) {
            ret = new Value(new FieldType.ObjectType("java/nio/charset/Charset"), new Value.Object());
        }
        else if (className.equals("java/nio/Bits") && method.getName().equals("byteOrder")) {
            ret = new Value(new FieldType.ObjectType("java/nio/ByteOrder"), new Value.Object());
        }
        // Stub the latter part of initializeSystemClass for now
        else if (className.equals("java/lang/System") && method.getName().equals("loadLibrary")) {
            ret = null;
        }
        else if (className.equals("java/lang/Terminator") && method.getName().equals("setup")) {
            ret = null;
        }
        else if (className.equals("java/lang/Thread") && method.getName().equals("getThreadGroup")) {
            ret = new Value(new FieldType.ObjectType("java/lang/ThreadGroup"), new Value.Object());
        }
        else if (className.equals("java/lang/ThreadGroup") && method.getName().equals("add")) {
            ret = null;
        }
        else if (className.equals("sun/misc/VM") && method.getName().equals("booted")) {
            ret = null;
        }
        // Current stub end of println:
        else if (className.equals("sun/nio/cs/StreamEncoder") && method.getName().equals("implWrite")) { // charset handling is broken
            Value.Object streamEncoder = (Value.Object)args[0].getValue();
            Value.Object printStream = (Value.Object)streamEncoder.getField("out").getValue();
            Value.Object bufferedOutputStream = (Value.Object)printStream.getField("out").getValue();
            Value.Object fileOutputStream = (Value.Object)bufferedOutputStream.getField("out").getValue();
            Value.Object fileDescriptor = (Value.Object)fileOutputStream.getField("fd").getValue();
            PrintStream stream = ((int)fileDescriptor.getField("fd").getValue() == 1) ? System.out : System.err;

            int off = (int)args[2].getValue();
            int len = (int)args[3].getValue();
            for (int i = off; i < off + len; i++) {
                stream.print(((char[])args[1].getValue())[i]);
            }
            ret = null;
        }
        else if ((className.equals("java/lang/Long") // getChars is broken
                || className.equals("java/lang/Float") // FloatingDecimal classfile parse may be failing
                || className.equals("java/lang/Double")) && method.getName().equals("toString")) {
            String str = args[0].getValue().toString();
            ret = new Value(new FieldType.ObjectType("java/lang/String"), new Value.Object(str));
        }
        else {
            isStub = false;
        }
        return new MethodStubResult(isStub, ret);
    }

    private static class NoMethodException extends RuntimeException {
        public NoMethodException(String message) {
            super(message);
        }
    }

    private static class MethodSearchResult {
        final Value.Class klass;
        final MethodInfo method;

        public MethodSearchResult(Value.Class klass, MethodInfo method) {
            this.klass = klass;
            this.method = method;
        }
    }

    private static class MethodStubResult {
        final boolean isStub;
        final Value value;

        public MethodStubResult(boolean isStub, Value value) {
            this.isStub = isStub;
            this.value = value;
        }
    }
}
