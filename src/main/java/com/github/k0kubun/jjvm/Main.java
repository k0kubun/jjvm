package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.bytecode.ClassFile;
import com.github.k0kubun.jjvm.bytecode.ClassFileParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ClassFile classFile = new ClassFileParser().parse("test/Hello.class");
        System.out.println(classFile.disassemble());
    }
}