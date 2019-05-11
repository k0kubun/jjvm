package com.github.k0kubun.jjvm.bytecode;

public class ConstantPoolInfo {
    private ConstantType type;

    public ConstantPoolInfo(ConstantType type) {
        this.type = type;
    }

    public ConstantType getType() {
        return type;
    }

    public static class Class extends ConstantPoolInfo {
        private int descriptorIndex;

        public Class(int descriptorIndex) {
            super(ConstantType.Class);
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class Fieldref extends ConstantPoolInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        public Fieldref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Fieldref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class Methodref extends ConstantPoolInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        public Methodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class InterfaceMethodref extends ConstantPoolInfo {
        private int classIndex;
        private int nameAndTypeIndex;

        public InterfaceMethodref(int classIndex, int nameAndTypeIndex) {
            super(ConstantType.Methodref);
            this.classIndex = classIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }

    public static class String extends ConstantPoolInfo {
        private int stringIndex;

        public String(int stringIndex) {
            super(ConstantType.String);
            this.stringIndex = stringIndex;
        }
    }

    public static class Integer extends ConstantPoolInfo {
        private int bytes;

        public Integer(int bytes) {
            super(ConstantType.Integer);
            this.bytes = bytes;
        }
    }

    public static class Float extends ConstantPoolInfo {
        private int bytes;

        public Float(int bytes) {
            super(ConstantType.Float);
            this.bytes = bytes;
        }
    }

    public static class Long extends ConstantPoolInfo {
        private int highBytes;
        private int lowBytes;

        public Long(int highBytes, int lowBytes) {
            super(ConstantType.Long);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class Double extends ConstantPoolInfo {
        private int highBytes;
        private int lowBytes;

        public Double(int highBytes, int lowBytes) {
            super(ConstantType.Double);
            this.highBytes = highBytes;
            this.lowBytes = lowBytes;
        }
    }

    public static class NameAndType extends ConstantPoolInfo {
        private int nameIndex;
        private int descriptorIndex;

        public NameAndType(int nameIndex, int descriptorIndex) {
            super(ConstantType.NameAndType);
            this.nameIndex = nameIndex;
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class Utf8 extends ConstantPoolInfo {
        private byte[] bytes;

        public Utf8(byte[] bytes) {
            super(ConstantType.Utf8);
            this.bytes = bytes;
        }
    }

    public static class MethodHandle extends ConstantPoolInfo {
        private int referenceKind;
        private int referenceIndex;

        public MethodHandle(int referenceKind, int referenceIndex) {
            super(ConstantType.MethodHandle);
            this.referenceKind = referenceKind;
            this.referenceIndex = referenceIndex;
        }
    }

    public static class MethodType extends ConstantPoolInfo {
        private int descriptorIndex;

        public MethodType(int descriptorIndex) {
            super(ConstantType.MethodType);
            this.descriptorIndex = descriptorIndex;
        }
    }

    public static class InvokeDynamic extends ConstantPoolInfo {
        private int bootstrapMethodAttrIndex;
        private int nameAndTypeIndex;

        public InvokeDynamic(int bootstrapMethodAttrIndex, int nameAndTypeIndex) {
            super(ConstantType.InvokeDynamic);
            this.bootstrapMethodAttrIndex = bootstrapMethodAttrIndex;
            this.nameAndTypeIndex = nameAndTypeIndex;
        }
    }
}
