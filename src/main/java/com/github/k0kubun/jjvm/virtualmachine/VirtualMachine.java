package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

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

    // Get or load a class from FieldType
    public ClassFile getClass(FieldType type) {
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

    // Load a ClassFile which is not loaded yet
    public void loadClass(ClassFile classFile) {
        classMap.put(classFile.getThisClassName(), classFile);
    }

    public void callStaticMethod(String className, String methodName) {
        ClassFile klass = classMap.get(className);
        MethodInfo method = searchMethod(klass, methodName);
        executeMethod(klass, method);
    }

    private ClassFile initializeClass(String klass) {
        ClassFile classFile = classLoader.loadClass(klass);
        loadClass(classFile);
        return classFile;
    }

    private MethodInfo searchMethod(ClassFile klass, String methodName) {
        for (MethodInfo method : klass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

    private void executeMethod(ClassFile klass, MethodInfo method) {
        AttributeInfo.Code code = (AttributeInfo.Code)method.getAttributes().get("Code");
        new BytecodeInterpreter(this, klass).execute(code);
    }
}
