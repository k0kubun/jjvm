package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
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
    }

    // Load a ClassFile which is not loaded yet
    public void loadClass(ClassFile classFile) {
        classMap.put(classFile.getThisClassName(), new Value.Class(classFile));
    }

    // Call an instance method
    public void callMethod(String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        Value.Class klass = getClass(args[0].getType());
        MethodInfo method = searchMethod(klass, methodName, methodType);
        executeMethod(klass, method, args);
    }

    public void callStaticMethod(String className, String methodName) {
        Value.Class klass = classMap.get(className);
        MethodInfo method = searchMethod(klass, methodName);
        executeMethod(klass, method, new Value[]{});
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

    private MethodInfo searchMethod(Value.Class klass, String methodName, MethodInfo.Descriptor methodType) {
        for (MethodInfo method : klass.getClassFile().getMethods()) {
            if (method.getName().equals(methodName) && method.getDescriptor().equals(methodType)) {
                return method;
            }
        }
        throw new RuntimeException(String.format("NoMethodError: %s.%s (%s)",
                klass.getClassFile().getThisClassName(), methodName, methodType.toString()));
    }

    // deprecated
    private MethodInfo searchMethod(Value.Class klass, String methodName) {
        for (MethodInfo method : klass.getClassFile().getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("NoMethodError: " + klass.getClassFile().getThisClassName() + "." + methodName);
    }

    private void executeMethod(Value.Class klass, MethodInfo method, Value[] args) {
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        new BytecodeInterpreter(this, klass).execute(code, args);
    }
}
