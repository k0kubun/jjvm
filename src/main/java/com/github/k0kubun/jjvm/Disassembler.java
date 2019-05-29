package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileDisassembler;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Disassembler {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: jjvmp [class file]");
            return;
        }

        try {
            ClassFile classFile = new ClassFileParser().parse(args[0]);
            System.out.println(new ClassFileDisassembler(classFile).disassemble());
        } catch (FileNotFoundException e) {
            System.out.println("Failed to open: " + e.getMessage());
        }
    }
}
