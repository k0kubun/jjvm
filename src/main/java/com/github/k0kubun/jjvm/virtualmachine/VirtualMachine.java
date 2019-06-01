package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
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

    // Load a ClassFile and return its class name.
    public void loadClass(ClassFile classFile) {
        classMap.put(classFile.getThisClassName(), classFile);
    }

    public void callStaticMethod(String className, String methodName) {
        ClassFile klass = classMap.get(className);
        MethodInfo method = searchMethod(klass, methodName);
        executeMethod(klass, method);
    }

    private void initializeClass(String klass) {
        loadClass(classLoader.loadClass(klass));
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
        new BytecodeInterpreter(klass).execute(code);
    }
}
