package com.github.k0kubun.jjvm.bytecode;

public class ClassFile {
    private int magic;
    private int minorVersion;
    private int majorVersion;
    private ConstantPoolInfo[] constantPool;

    public ClassFile(int magic, int minorVersion, int majorVersion, ConstantPoolInfo[] constantPool) {
        this.magic = magic;
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constantPool = constantPool;
    }

    public String disassemble() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("class Hello\n"));
        builder.append(String.format("  magic: 0x%X\n", magic));
        builder.append(String.format("  minor version: %d\n", minorVersion));
        builder.append(String.format("  major version: %d\n", majorVersion));
        builder.append(String.format("  flags: TODO\n"));
        builder.append(disassembleConstantPool());
        return builder.toString();
    }

    private String disassembleConstantPool() {
        StringBuilder builder = new StringBuilder();
        builder.append("Constant pool:\n");
        for (int i = 0; i < constantPool.length; i++) {
            String typeName = constantPool[i].getType().toString();
            builder.append(String.format("%5s = %-19s", String.format("#%d", i + 1), typeName));
            builder.append("TODO");
            builder.append("\n");
        }
        return builder.toString();
    }
}
