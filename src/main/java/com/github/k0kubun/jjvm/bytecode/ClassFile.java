package com.github.k0kubun.jjvm.bytecode;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class ClassFile {
    private final int minorVersion;
    private final int majorVersion;
    private final ConstantInfo[] constantPool;
    private final List<AccessFlag> accessFlags;
    private final int thisClass;
    private final int superClass;
    private final int[] interfaces;
    private final FieldInfo[] fields;
    private final MethodInfo[] methods;
    private final AttributeInfo[] attributes;

    private final static int MAGIC = 0xCAFEBABE;

    public ClassFile(int magic, int minorVersion, int majorVersion, ConstantInfo[] constantPool, int accessFlags, int thisClass,
                     int superClass, int[] interfaces, FieldInfo[] fields, MethodInfo[] methods, AttributeInfo[] attributes) {
        if (magic != MAGIC) {
            throw new RuntimeException(String.format("unexpected magic: 0x%X", magic));
        }
        this.minorVersion = minorVersion;
        this.majorVersion = majorVersion;
        this.constantPool = constantPool;
        this.accessFlags = AccessFlag.fromInt(accessFlags);
        this.thisClass = thisClass;
        this.superClass = superClass;
        this.interfaces = interfaces;
        this.fields = fields;
        this.methods = methods;
        this.attributes = attributes;
    }

    public String disassemble() {
        StringJoiner flags = new StringJoiner(", ");
        accessFlags.stream().forEach(f -> flags.add(f.toString()));

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("class %s\n", utf8Constant(classConstant(thisClass).getDescriptorIndex()).getString()));
        builder.append(String.format("  minor version: %d\n", minorVersion));
        builder.append(String.format("  major version: %d\n", majorVersion));
        builder.append(String.format("  flags: %s\n", flags.toString()));
        builder.append(disassembleConstantPool());
        builder.append(disassembleMethods());
        return builder.toString();
    }

    private String disassembleConstantPool() {
        StringBuilder builder = new StringBuilder();
        builder.append("Constant pool:\n");
        for (int i = 0; i < constantPool.length; i++) {
            ConstantType type = constantPool[i].getType();
            builder.append(String.format("%5s = %-19s", String.format("#%d", i + 1), type.toString()));

            if (type == ConstantType.Class) {
                int index = ((ConstantInfo.Class)constantPool[i]).getDescriptorIndex();
                builder.append(String.format("#%-14d// %s", index, utf8Constant(index).getString()));
            } else if (type == ConstantType.Utf8) {
                builder.append(((ConstantInfo.Utf8)constantPool[i]).getString());
            } else {
                builder.append("[TODO]");
            }

            builder.append("\n");
        }
        return builder.toString();
    }

    private String disassembleMethods() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (MethodInfo method : methods) {
            StringJoiner declaration = new StringJoiner(" ");
            method.getAccessFlags().stream().forEach(f -> declaration.add(f.getName()));
            declaration.add(method.getName());
            builder.append(String.format("\n  %s();\n", declaration.toString()));

            builder.append(String.format("    descriptor: %s\n", method.getDescriptor()));

            StringJoiner flags = new StringJoiner(", ");
            method.getAccessFlags().stream().forEach(f -> flags.add(f.toString()));
            builder.append(String.format("    flags: %s\n", flags.toString()));

            for (AttributeInfo attribute : method.getAttributes()) {
                builder.append(String.format("    %s:\n", attribute.getAttributeName()));
            }
        }
        builder.append("}\n");
        return builder.toString();
    }

    private ConstantInfo.Class classConstant(int index) {
        return (ConstantInfo.Class)constant(index);
    }

    private ConstantInfo.Utf8 utf8Constant(int index) {
        return (ConstantInfo.Utf8)constant(index);
    }

    private ConstantInfo constant(int index) {
        return constantPool[index - 1];
    }

    public enum AccessFlag {
        ACC_PUBLIC(0x0001),
        ACC_FINAL(0x0010),
        ACC_SUPER(0x0020),
        ACC_INTERFACE(0x0200),
        ACC_ABSTRACT(0x0400),
        ACC_SYNTHETIC(0x1000),
        ACC_ANNOTATION(0x2000),
        ACC_ENUM(0x4000);

        private final int value;

        AccessFlag(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public String getName() {
            String suffix = toString().substring(4); // trim ACC_
            return suffix.toLowerCase();
        }

        public static List<AccessFlag> fromInt(int accessFlags) {
            List<AccessFlag> list = new ArrayList<>();
            for (AccessFlag flag : AccessFlag.values()) {
                if ((flag.getValue() & accessFlags) != 0) {
                    list.add(flag);
                }
            }
            return list;
        }
    }
}
