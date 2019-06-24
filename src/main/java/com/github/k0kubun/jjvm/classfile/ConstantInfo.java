package com.github.k0kubun.jjvm.classfile;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ConstantInfo {
    private final ConstantType type;

    ConstantInfo(ConstantType type) {
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

    public static class Class extends ConstantInfo {
        private final int descriptorIndex;
        private java.lang.String name;

        Class(int descriptorIndex) {
            super(ConstantType.Class);
            this.descriptorIndex = descriptorIndex;
        }

        public java.lang.String getName() {
            return name;
        }

        void setName(java.lang.String name) {
            this.name = name;
        }

        int getNameIndex() {
            return descriptorIndex;
        }
    }

    public static class Fieldref extends ConstantInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;
        private ConstantInfo.Class classInfo;
        private ConstantInfo.NameAndType nameAndType;

        Fieldref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Fieldref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public ConstantInfo.Class getClassInfo() {
            return classInfo;
        }

        public ConstantInfo.NameAndType getNameAndType() {
            return nameAndType;
        }

        int getClassIndex() {
            return classIndex;
        }

        void setClassInfo(ConstantInfo.Class classInfo) {
            this.classInfo = classInfo;
        }

        int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }

        void setNameAndType(ConstantInfo.NameAndType nameAndType) {
            this.nameAndType = nameAndType;
        }
    }

    public static class Methodref extends ConstantInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;
        private ConstantInfo.Class classInfo;
        private ConstantInfo.NameAndType nameAndType;

        Methodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }

        public ConstantInfo.Class getClassInfo() {
            return classInfo;
        }

        public ConstantInfo.NameAndType getNameAndType() {
            return nameAndType;
        }

        int getClassIndex() {
            return classIndex;
        }

        void setClassInfo(ConstantInfo.Class classInfo) {
            this.classInfo = classInfo;
        }

        int getNameAndTypeIndex() {
            return nameAndTypeIndex;
        }

        void setNameAndType(ConstantInfo.NameAndType nameAndType) {
            this.nameAndType = nameAndType;
        }
    }

    public static class InterfaceMethodref extends ConstantInfo {
        private final int classIndex;
        private final int nameAndTypeIndex;

        InterfaceMethodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class String extends ConstantInfo {
        private final int stringIndex;
        private java.lang.String string;

        String(int stringIndex) {
            super(ConstantType.String);
            this.stringIndex = stringIndex;
        }

        public java.lang.String getString() {
            return string;
        }

        int getStringIndex() {
            return stringIndex;
        }

        void setString(java.lang.String string) {
            this.string = string;
        }
    }

    public static class Integer extends ConstantInfo {
        private final int bytes;

        Integer(int bytes) {
            super(ConstantType.Integer);
            this.bytes = bytes;
        }
    }

    public static class Float extends ConstantInfo {
        private final float value;

        Float(byte[] bytes) {
            super(ConstantType.Float);
            this.value = ByteBuffer.wrap(bytes).getFloat();
        }

        public float getValue() {
            return value;
        }
    }

    public static class Long extends ConstantInfo {
        private final long value;

        Long(long value) {
            super(ConstantType.Long);
            this.value = value;
        }

        public long getValue() {
            return value;
        }
    }

    public static class Double extends ConstantInfo {
        private final double value;

        Double(byte[] bytes) {
            super(ConstantType.Double);
            this.value = ByteBuffer.wrap(bytes).getDouble();
        }

        public double getValue() {
            return value;
        }
    }

    public static class NameAndType extends ConstantInfo {
        private final int nameIndex;
        private final int descriptorIndex;
        private java.lang.String name;
        private java.lang.String descriptor;

        NameAndType(int nameIndex, int descriptorIndex) {
            super(ConstantType.NameAndType);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }

        public java.lang.String getName() {
            return name;
        }

        public MethodInfo.Descriptor getMethodDescriptor() {
            return ClassFileParser.DescriptorParser.parseMethod(descriptor);
        }

        int getNameIndex() {
            return nameIndex;
        }

        void setName(java.lang.String name) {
            this.name = name;
        }

        int getDescriptorIndex() {
            return descriptorIndex;
        }

        void setDescriptor(java.lang.String descriptor) {
            this.descriptor = descriptor;
        }
    }

    public static class Utf8 extends ConstantInfo {
        private final byte[] bytes;

        Utf8(byte[] bytes) {
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

        MethodHandle(int referenceKind, int referenceIndex) {
            super(ConstantType.MethodHandle);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
        }
    }

    public static class MethodType extends ConstantInfo {
        private final int descriptorIndex;

        MethodType(int descriptorIndex) {
            super(ConstantType.MethodType);
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class InvokeDynamic extends ConstantInfo {
        private final int bootstrapMethodAttrIndex;
        private final int nameAndTypeIndex;

        InvokeDynamic(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
            super(ConstantType.InvokeDynamic);
            this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
}
