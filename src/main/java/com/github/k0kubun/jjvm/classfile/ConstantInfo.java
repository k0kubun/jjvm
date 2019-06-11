package com.github.k0kubun.jjvm.classfile;

import java.nio.charset.StandardCharsets;

public class ConstantInfo {
    private final ConstantType type;

    public ConstantInfo(ConstantType type) {
        this.type = type;
    }

    public ConstantType getType() {
        return type;
    }

    public interface NamedInfo {
        int getNameIndex();
    }

    public interface ClassRefInfo {
        int getClassIndex();
        int getNameAndTypeIndex();
    }

    public static class Class extends ConstantInfo implements NamedInfo {
        private final int descriptorIndex;

        public Class(int descriptorIndex) {
            super(ConstantType.Class);
            this.descriptorIndex = descriptorIndex;
        }

        @Override
        public int getNameIndex() {
            return descriptorIndex;
        }
    }

    public static class Fieldref extends ConstantInfo implements ClassRefInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        public Fieldref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Fieldref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        @Override
        public int getClassIndex() {
            return classIndex;
        }

        @Override
        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    public static class Methodref extends ConstantInfo implements ClassRefInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        public Methodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        @Override
        public int getClassIndex() {
            return classIndex;
        }

        @Override
        public int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }
    }

    public static class InterfaceMethodref extends ConstantInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        public InterfaceMethodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class String extends ConstantInfo implements NamedInfo {
        private final int stringIndex;

        public String(int stringIndex) {
            super(ConstantType.String);
            this.stringIndex = stringIndex;
        }

        @Override
        public int getNameIndex() {
            return stringIndex;
        }
    }

    public static class Integer extends ConstantInfo {
        private final int bytes;

        public Integer(int bytes) {
            super(ConstantType.Integer);
            this.bytes = bytes;
        }
    }

    public static class Float extends ConstantInfo {
        private final int bytes;

        public Float(int bytes) {
            super(ConstantType.Float);
            this.bytes = bytes;
        }
    }

    public static class Long extends ConstantInfo {
        private final long value;

        public Long(long value) {
            super(ConstantType.Long);
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    public static class Double extends ConstantInfo {
        private final int highBytes;
        private final int lowBytes;

        public Double(int highBytes, int lowBytes) {
            super(ConstantType.Double);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class NameAndType extends ConstantInfo implements NamedInfo {
        private final int nameIndex;
        private final int descriptorIndex;

        public NameAndType(int nameIndex, int descriptorIndex) {
            super(ConstantType.NameAndType);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        @Override
        public int getNameIndex() {
            return nameIndex;
        }

        public int getDescriptorIndex() {
            return descriptorIndex;
        }
    }

    public static class Utf8 extends ConstantInfo {
        private final byte[] bytes;

        public Utf8(byte[] bytes) {
            super(ConstantType.Utf8);
            this.bytes = bytes;
        }

        public java.lang.String getString() {
            return new java.lang.String(bytes, StandardCharsets.UTF_8);
        }
    }

    public static class MethodHandle extends ConstantInfo {
        private final int referenceKind;
        private final int referenceIndex;

        public MethodHandle(int referenceKind, int referenceIndex) {
            super(ConstantType.MethodHandle);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
        }
    }

    public static class MethodType extends ConstantInfo {
        private final int descriptorIndex;

        public MethodType(int descriptorIndex) {
            super(ConstantType.MethodType);
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class InvokeDynamic extends ConstantInfo {
        private final int bootstrapMethodAttrIndex;
        private final int nameAndTypeIndex;

        public InvokeDynamic(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
            super(ConstantType.InvokeDynamic);
            this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
}
