package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.bytecode.ClassFile;
import com.github.k0kubun.jjvm.bytecode.ClassParser;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        ClassFile classFile = new ClassParser().parseClassFile("test/Hello.class");
        System.out.println(classFile.disassemble());
    }
}
