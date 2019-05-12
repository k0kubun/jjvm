package com.github.k0kubun.jjvm.bytecode;

public class FieldType implements MethodInfo.ReturnDescriptor {
    private final String type;

    public FieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    public static class BaseType extends FieldType {
        public BaseType(String type) {
            super(type);
        }
    }

    public static class ObjectType extends FieldType {
        public ObjectType(String type) {
            super(type);
        }
    }

    public static class ArrayType extends FieldType {
        private final FieldType componentType;

        public ArrayType(FieldType componentType) {
            super(componentType.getType() + "[]");
            this.componentType = componentType;
        }
    }
}
