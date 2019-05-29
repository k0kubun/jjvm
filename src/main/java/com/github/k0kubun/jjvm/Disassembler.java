package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileDisassembler;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.IOException;

// $ jjvmp [class file]
public class Disassembler {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: jjvmp [class file]");
            return;
        }

        ClassFile classFile;
        try {
            classFile = new ClassFileParser().parse(args[0]);
        } catch (IOException e) {
            System.out.println("Failed to open: " + e.getMessage());
            return;
        }

        String disasm = new ClassFileDisassembler(classFile).disassemble();
        System.out.println(disasm);
    }
}
