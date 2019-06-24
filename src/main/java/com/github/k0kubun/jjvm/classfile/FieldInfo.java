package com.github.k0kubun.jjvm.classfile;

import java.util.HashSet;
import java.util.Set;

public class FieldInfo {
    private final Set<AccessFlag> accessFlags;
    private final String name;
    private final FieldType descriptor;
    private final AttributeInfo[] attributes; // not used yet

    FieldInfo(int accessFlags, int nameIndex, int descriptorIndex, AttributeInfo[] attributes, ConstantInfo[] constantPool) {
        this.accessFlags = AccessFlag.fromInt(accessFlags);
        this.name = ((ConstantInfo.Utf8)constantPool[nameIndex - 1]).getString();
        this.descriptor = ClassFileParser.DescriptorParser.parseField(
                ((ConstantInfo.Utf8)constantPool[descriptorIndex - 1]).getString());
        this.attributes = attributes;
    }

    public Set<AccessFlag> getAccessFlags() {
        return accessFlags;
    }

    public String getName() {
        return name;
    }

    public FieldType getDescriptor() {
        return descriptor;
    }

    public enum AccessFlag {
        ACC_PUBLIC(0x0001),
        ACC_PRIVATE(0x0002),
        ACC_PROTECTED(0x0004),
        ACC_STATIC(0x0008),
        ACC_FINAL(0x0010),
        ACC_VOLATILE(0x0040),
        ACC_TRANSIENT(0x0080),
        ACC_SYNTHETIC(0x1000),
        ACC_ENUM(0x4000);

        private final int value;

        AccessFlag(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        public static Set<AccessFlag> fromInt(int accessFlags) {
            Set<AccessFlag> flags = new HashSet<>();
            for (AccessFlag flag : AccessFlag.values()) {
                if ((flag.getValue() & accessFlags) != 0) {
                    flags.add(flag);
                }
            }
            return flags;
        }
    }
}
