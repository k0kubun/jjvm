package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.AttributeInfo;
import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

import java.util.HashMap;
import java.util.Map;

public class VirtualMachine {
    private final Map<String, ClassFile> classMap;

    public VirtualMachine() {
        classMap = new HashMap<>();
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
