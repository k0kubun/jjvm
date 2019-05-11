package com.github.k0kubun.jjvm.bytecode;

public class AttributeInfo {
    private final String attributeName;
    private final int[] info;

    public AttributeInfo(String attributeName, int[] info) {
        this.attributeName = attributeName;
        this.info = info;
    }

    public String getAttributeName() {
        return attributeName;
    }
}
