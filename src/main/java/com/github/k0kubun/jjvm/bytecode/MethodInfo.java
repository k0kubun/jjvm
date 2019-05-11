package com.github.k0kubun.jjvm.bytecode;

public class MethodInfo {
    private final int accessFlags;
    private final String name;
    private final String descriptor;
    private final AttributeInfo[] attributes;

    public MethodInfo(int accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        this.accessFlags = accessFlags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public String getName() {
        return this.name;
    }

    public String getDescriptor() {
        return this.descriptor;
    }

    public AttributeInfo[] getAttributes() {
        return this.attributes;
    }
}
