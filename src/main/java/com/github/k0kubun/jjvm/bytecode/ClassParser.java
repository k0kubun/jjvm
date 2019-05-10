package com.github.k0kubun.jjvm.bytecode;

public class ClassParser {
    public void parseFile(String filename) {
        readFile(filename);
    }

    private void readFile(String filename) {
        System.out.println(filename);
    }
}
