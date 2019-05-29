package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;
import com.github.k0kubun.jjvm.virtualmachine.VirtualMachine;

import java.io.IOException;

// $ jjvm [class file]
public class JJVM {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: jjvm [class file]");
            return;
        }

        ClassFile classFile;
        try {
            classFile = new ClassFileParser().parse(args[0]);
        } catch (IOException e) {
            System.out.println("Failed to open: " + e.getMessage());
            return;
        }

        VirtualMachine vm = new VirtualMachine();
        vm.loadClass(classFile);
        vm.callStaticMethod(classFile.getThisClassName(), "main");
    }
}
