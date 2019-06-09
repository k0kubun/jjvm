package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

import java.util.HashMap;
import java.util.Map;

// A class hodling VM states and providing VM-related interfaces.
public class VirtualMachine {
    private final Map<String, Value.Class> classMap;
    private final ClassLoader classLoader;

    // Threads::create_vm() equivalent
    public VirtualMachine() {
        classMap = new HashMap<>();
        classLoader = new ClassLoader();

        // initializeClass("java/lang/String");
        initializeClass("java/lang/System");

        callInitializeSystemClass();
    }

    // Load a ClassFile which is not loaded yet
    public Value.Class loadClass(ClassFile classFile) {
        Value.Class klass = new Value.Class(classFile);
        classMap.put(classFile.getThisClassName(), klass);
        return klass;
    }

    // Call an instance method
    public Value callMethod(String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(args[0].getType());
        MethodInfo method = searchMethod(klass, methodName, methodType);
        return executeMethod(klass, method, args);
    }

    public Value callStaticMethod(Value.Class klass, String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        MethodInfo method = searchMethod(klass, methodName, methodType);
        return executeMethod(klass, method, args);
    }

    public Value.Class getClass(String name) {
        return getClass(fieldType(String.format("L%s;", name)));
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

    private Value.Class initializeClass(String klass) {
        ClassFile classFile = classLoader.loadClass(klass);
        loadClass(classFile);
        return new Value.Class(classFile);
    }

    // `call_initializeSystemClass` equivalent
    private void callInitializeSystemClass() {
        Value.Class system = classMap.get("java/lang/System");
        system.setField("out", new Value(fieldType("Ljava/io/PrintStream;"), new Value.Object()));
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
        throw new RuntimeException(String.format("NoMethodError: %s.%s (%s)",
                klass.getClassFile().getThisClassName(), methodName, methodType.toString()));
    }

    private Value executeMethod(Value.Class klass, MethodInfo method, Value[] args) {
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        return new BytecodeInterpreter(this, klass).execute(code, args);
    }
}
