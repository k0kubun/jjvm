package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
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
            return new Value(new FieldType.Int(), 0);
        } else if (fieldType instanceof FieldType.Long) {
            return new Value(new FieldType.Long(), 0L);
        } else if (fieldType instanceof FieldType.Float) {
            return new Value(new FieldType.Double(), +0.0F);
        } else if (fieldType instanceof FieldType.Double) {
            return new Value(new FieldType.Double(), +0.0D);
        } else if (fieldType instanceof FieldType.Boolean) {
            return new Value(new FieldType.Boolean(), false);
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

        MethodInfo.Descriptor clinitType = ClassFileParser.DescriptorParser.parseMethod("()V");
        try {
            MethodSearchResult result = searchMethod(value, "<clinit>", clinitType);
            executeMethod(result.klass, result.method, new Value[0]);
        } catch (NoMethodException e) {
            // ignore undefined <clinit>:()V call
        }
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
        //MethodSearchResult result = searchMethod(system, "initializeSystemClass",
        //        ClassFileParser.DescriptorParser.parseMethod("()V"));
        //executeMethod(result.klass, result.method, new Value[0]);
        system.setField("out", new Value(fieldType("Ljava/io/PrintStream;"), new Value.Object()));
        system.setField("err", new Value(fieldType("Ljava/io/PrintStream;"), new Value.Object()));
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
        //System.out.println(klass.getClassFile().getThisClassName() + "." + method.getName());
        if (method.getAccessFlags().contains(MethodInfo.AccessFlag.ACC_NATIVE)) {
            // TODO: Carve out this logic
            String className = klass.getClassFile().getThisClassName();
            if (className.equals("java/lang/System") && method.getName().equals("arraycopy")) {
                System.arraycopy(args[0].getValue(), (Integer) args[1].getValue(),
                        args[2].getValue(), (Integer) args[3].getValue(), (Integer) args[4].getValue());
                return null;
            } else if (method.getName().equals("registerNatives")) {
                if (className.equals("java/lang/System")
                        || className.equals("java/lang/Object")
                        || className.equals("java/lang/Class")
                        || className.equals("java/lang/ClassLoader")) {
                    // nothing registered for now
                } else {
                    throw new RuntimeException("Unsupported native method: " + klass.getClassFile().getThisClassName() + "." + method.getName());
                }
                return null;
            } else if (className.equals("java/lang/System") && method.getName().equals("initProperties")) {
                // not implemented properly yet. FIXME: implement something
                return Value.Null();
            } else if (className.equals("sun/misc/VM") && method.getName().equals("initialize")) {
                // not implemented properly yet. FIXME: implement something
                return null;
            } else if (className.equals("java/lang/Class") && method.getName().equals("desiredAssertionStatus0")) {
                // not implemented properly yet. FIXME: Is it okay?
                return new Value(new FieldType.Boolean(), true);
            } else if (className.equals("java/lang/Class") && method.getName().equals("getPrimitiveClass")) {
                // not implemented properly yet. FIXME: implement something
                return Value.Null();
            } else if (className.equals("java/lang/Shutdown") && method.getName().equals("halt0")) {
                System.exit((int)args[0].getValue());
                return null;
            } else if (className.equals("java/lang/Float") && method.getName().equals("floatToRawIntBits")) {
                int result = Float.floatToRawIntBits((Float)args[0].getValue());
                return new Value(new FieldType.Int(), result);
            } else if (className.equals("java/lang/Double") && method.getName().equals("doubleToRawLongBits")) {
                long result = Double.doubleToRawLongBits((Double)args[0].getValue());
                return new Value(new FieldType.Long(), result);
            } else if (className.equals("java/lang/Double") && method.getName().equals("longBitsToDouble")) {
                double result = Double.longBitsToDouble((Long)args[0].getValue());
                return new Value(new FieldType.Double(), result);
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

    private static class MethodSearchResult {
        final Value.Class klass;
        final MethodInfo method;

        public MethodSearchResult(Value.Class klass, MethodInfo method) {
            this.klass = klass;
            this.method = method;
        }
    }
}
