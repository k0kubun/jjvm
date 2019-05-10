package com.github.k0kubun.jjvm.bytecode;

public class ClassFile {
    private Integer magic;
    private Integer minorVersion;
    private Integer majorVersion;
    private Integer constantPoolCount;

    public ClassFile(Integer magic, Integer minorVersion, Integer majorVersion, Integer constantPoolCount) {
        this.magic = magic;
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constantPoolCount = constantPoolCount;
    }

    public String disassemble() {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("class Hello\n"));
        builder.append(String.format("  magic: 0x%X\n", magic));
        builder.append(String.format("  minor version: %d\n", minorVersion));
        builder.append(String.format("  major version: %d\n", majorVersion));
        return builder.toString();
    }
}
