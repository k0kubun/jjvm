package com.github.k0kubun.jjvm.bytecode;

public class ClassFile {
    private final int magic;
    private final int minorVersion;
    private final int majorVersion;
    private final ConstantPoolInfo[] constantPool;
    private final int accessFlags;
    private final int thisClass;
    private final int superClass;
    private final int[] interfaces;
    private final FieldInfo[] fields;
    private final MethodInfo[] methods;

    public ClassFile(int magic, int minorVersion, int majorVersion, ConstantPoolInfo[] constantPool, int accessFlags,
                     int thisClass, int superClass, int[] interfaces, FieldInfo[] fields, MethodInfo[] methods) {
        this.magic = magic;
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constantPool = constantPool;
        this.accessFlags = accessFlags;
        this.thisClass = thisClass;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
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
