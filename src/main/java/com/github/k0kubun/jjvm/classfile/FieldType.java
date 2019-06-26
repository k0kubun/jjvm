package com.github.k0kubun.jjvm.classfile;

public class FieldType implements MethodInfo.ReturnDescriptor {
    private final String type;

    FieldType(String type) {
        this.type = type;
    }

    public String getType() {
        return this.type;
    }

    // B	byte	signed byte
    // C	char	Unicode character code point in the Basic Multilingual Plane, encoded with UTF-16
    // D	double	double-precision floating-point value
    // F	float	single-precision floating-point value
    // I	int	    integer
    // J	long	long integer
    // S	short	signed short
    // Z	boolean	true or false
    static class BaseType extends FieldType {
        BaseType(String type) {
            super(type);
        }
    }

    public static final Byte BYTE = new Byte();
    public static class Byte extends BaseType {
        public Byte() {
            super("byte");
        }
    }

    public static final Char CHAR = new Char();
    public static class Char extends BaseType {
        public Char() {
            super("char");
        }
    }

    public static final Double DOUBLE = new Double();
    public static class Double extends BaseType {
        public Double() {
            super("double");
        }
    }

    public static final Float FLOAT = new Float();
    public static class Float extends BaseType {
        public Float() {
            super("float");
        }
    }

    public static final Int INT = new Int();
    public static class Int extends BaseType {
        public Int() {
            super("int");
        }
    }

    public static final Long LONG = new Long();
    public static class Long extends BaseType {
        public Long() {
            super("long");
        }
    }

    public static final Short SHORT = new Short();
    public static class Short extends BaseType {
        public Short() {
            super("short");
        }
    }

    public static final Boolean BOOLEAN = new Boolean();
    public static class Boolean extends BaseType {
        public Boolean() {
            super("boolean");
        }
    }

    public static class ObjectType extends FieldType {
        private final String className;

        public ObjectType(String type) {
            super(type.replace("/", "."));
            className = type;
        }

        public String getClassName() {
            return className;
        }
    }

    public static class ArrayType extends FieldType {
        private final FieldType componentType;

        public ArrayType(FieldType componentType) {
            super(componentType.getType() + "[]");
            this.componentType = componentType;
        }

        public FieldType getComponentType() {
            return componentType;
        }
    }
}
