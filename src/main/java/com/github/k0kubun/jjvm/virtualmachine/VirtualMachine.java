package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;
import com.github.k0kubun.jjvm.virtualmachine.BytecodeInterpreter.Value;

import java.util.HashMap;
import java.util.Map;

// A class hodling VM states and providing VM-related interfaces.
public class VirtualMachine {
    private final Map<String, ClassFile> classMap;
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
        classMap.put(classFile.getThisClassName(), classFile);
    }

    // Call an instance method
    public void callMethod(String methodName, MethodInfo.Descriptor methodType, Value[] args) {
        ClassFile klass = getClass(args[0].getType());
        MethodInfo method = searchMethod(klass, methodName, methodType);
        executeMethod(klass, method, args);
    }

    public void callStaticMethod(String className, String methodName) {
        ClassFile klass = classMap.get(className);
        MethodInfo method = searchMethod(klass, methodName);
        executeMethod(klass, method, new Value[]{});
    }

    // Get or load a class from FieldType
    private ClassFile getClass(FieldType type) {
        if (type instanceof FieldType.ObjectType) {
            FieldType.ObjectType objectType = (FieldType.ObjectType)type;
            if (classMap.containsKey(objectType.getClassName())) {
                return classMap.get(objectType.getClassName());
            } else {
                return initializeClass(objectType.getClassName());
            }
        } else {
            throw new RuntimeException("unexpected FieldType is given to getClass: " + type.getType());
        }
    }

    private ClassFile initializeClass(String klass) {
        ClassFile classFile = classLoader.loadClass(klass);
        loadClass(classFile);
        return classFile;
    }

    private MethodInfo searchMethod(ClassFile klass, String methodName, MethodInfo.Descriptor methodType) {
        for (MethodInfo method : klass.getMethods()) {
            if (method.getName().equals(methodName) && method.getDescriptor().equals(methodType)) {
                return method;
            }
        }
        throw new RuntimeException(String.format("NoMethodError: %s.%s (%s)", klass.getThisClassName(), methodName, methodType.toString()));
    }

    // deprecated
    private MethodInfo searchMethod(ClassFile klass, String methodName) {
        for (MethodInfo method : klass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new RuntimeException("NoMethodError: " + klass.getThisClassName() + "." + methodName);
    }

    private void executeMethod(ClassFile klass, MethodInfo method, Value[] args) {
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        new BytecodeInterpreter(this, klass).execute(code, args);
    }
}
