package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.bytecode.ClassParser;

public class Main {
    public static void main(String[] args) {
        parseClassFile("test/Hello.class");
    }

    private static void parseClassFile(String filename) {
        new ClassParser().parseFile(filename);
    }
}
