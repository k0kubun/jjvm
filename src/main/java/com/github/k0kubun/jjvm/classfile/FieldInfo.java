package com.github.k0kubun.jjvm.classfile;

public class FieldInfo {
    private final int accessFlags; // not used yet
    private final String name;
    private final FieldType descriptor;
    private final AttributeInfo[] attributes; // not used yet

    FieldInfo(int accessFlags, int nameIndex, int descriptorIndex, AttributeInfo[] attributes, ConstantInfo[] constantPool) {
        this.accessFlags = accessFlags;
        this.name = ((ConstantInfo.Utf8)constantPool[nameIndex - 1]).getString();
        this.descriptor = ClassFileParser.DescriptorParser.parseField(
                ((ConstantInfo.Utf8)constantPool[descriptorIndex - 1]).getString());
        this.attributes = attributes;
    }

    public String getName() {
        return name;
    }

    public FieldType getDescriptor() {
        return descriptor;
    }
}
