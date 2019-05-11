package com.github.k0kubun.jjvm.bytecode;

import java.util.ArrayList;
import java.util.List;

public class MethodInfo {
    private final List<AccessFlag> accessFlags;
    private final String name;
    private final String descriptor;
    private final AttributeInfo[] attributes;

    public MethodInfo(int accessFlags, String name, String descriptor, AttributeInfo[] attributes) {
        List<AccessFlag> flags = new ArrayList<>();
        for (AccessFlag flag : AccessFlag.values()) {
            if ((flag.getValue() & accessFlags) != 0) {
                flags.add(flag);
            }
        }
        this.accessFlags = flags;
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = attributes;
    }

    public List<AccessFlag> getAccessFlags() {
        return accessFlags;
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

    public enum AccessFlag {
        ACC_PUBLIC(0x0001),
        ACC_PRIVATE(0x0002),
        ACC_PROTECTED(0x0004),
        ACC_STATIC(0x0008),
        ACC_FINAL(0x0010),
        ACC_SYNCHRONIZED(0x0020),
        ACC_BRIDGE(0x0040),
        ACC_VARARGS(0x0080),
        ACC_NATIVE(0x0100),
        ACC_ABSTRACT(0x0400),
        ACC_STRICT(0x0800),
        ACC_SYNTHETIC(0x1000);

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
    }
}
