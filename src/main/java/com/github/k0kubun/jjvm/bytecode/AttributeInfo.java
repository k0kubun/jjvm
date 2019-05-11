package com.github.k0kubun.jjvm.bytecode;

public class AttributeInfo {
    private final int attributeNameIndex;
    private final int[] info;

    public AttributeInfo(int attributeNameIndex, int[] info) {
        this.attributeNameIndex = attributeNameIndex;
        this.info = info;
    }
}
