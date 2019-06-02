package com.github.k0kubun.jjvm.classfile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MethodInfo {
    private final List<AccessFlag> accessFlags;
    private final String name;
    private final Descriptor descriptor;
    private final Map<String, AttributeInfo> attributes;

    public MethodInfo(int accessFlags, String name, Descriptor descriptor, AttributeInfo[] attributes) {
        this.accessFlags = AccessFlag.fromInt(accessFlags);
        this.name = name;
        this.descriptor = descriptor;
        this.attributes = new HashMap<>();
        for (AttributeInfo attribute : attributes) {
            this.attributes.put(attribute.getName(), attribute);
        }
    }

    public List<AccessFlag> getAccessFlags() {
        return accessFlags;
    }

    public String getName() {
        return this.name;
    }

    public Descriptor getDescriptor() {
        return this.descriptor;
    }

    public Map<String, AttributeInfo> getAttributes() {
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

    public interface ReturnDescriptor {
        String getType();
    }

    public static class VoidDescriptor implements ReturnDescriptor {
        public String getType() {
            return "void";
        }
    }

    public static class Descriptor {
        private final String raw;
        private final ReturnDescriptor returnDescriptor;
        private final List<FieldType> parameters;

        public Descriptor(String raw, ReturnDescriptor returnDescriptor, List<FieldType> parameters) {
            this.raw = raw;
            this.returnDescriptor = returnDescriptor;
            this.parameters = parameters;
        }

        public String toString() {
            return raw;
        }

        public ReturnDescriptor getReturn() {
            return returnDescriptor;
        }

        public List<FieldType> getParameters() {
            return parameters;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Descriptor && raw.equals(other.toString());
        }

        @Override
        public int hashCode() {
            return raw.hashCode();
        }
    }
}
