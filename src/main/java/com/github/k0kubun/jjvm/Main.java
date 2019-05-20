package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileDisassembler;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ClassFile classFile = new ClassFileParser().parse("test/Hello.class");
        System.out.println(new ClassFileDisassembler(classFile).disassemble());
    }
}
