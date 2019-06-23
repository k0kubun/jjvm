package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.FieldType;

import java.util.HashMap;
import java.util.Map;

// A container of any JVM value. The contained value may NOT be an object.
public class Value {
    private final FieldType type;

    // `value` should have a value serialized depending on the `type`. The caller of
    // getValue() is responsible for deserializing the `value` depending on the `type`.
    //   I                   => Integer
    //   [I                  => int[]
    //   Ljava/lang/String;  => Value.Object
    //   [Ljava/lang/String; => Value.Object[]
    //   Lfoo/bar;           => Value.Object
    //   [Lfoo/bar;          => Value.Object[]
    //   Ljava/lang/Class;   => Value.Class
    //   null (of any class) => null
    private final java.lang.Object value;

    public static Value Null() {
        return new Value((FieldType)null, (Object)null);
    }

    public Value(FieldType.Boolean type, boolean value) {
        this(type, (Boolean)value);
    }

    public Value(FieldType.Int type, int value) {
        this(type, (Integer)value);
    }

    public Value(FieldType.Long type, long value) {
        this(type, (Long)value);
    }

    public Value(FieldType.Float type, float value) {
        this(type, (Float)value);
    }

    public Value(FieldType.Double type, double value) {
        this(type, (Double)value);
    }

    public Value(FieldType.Char type, char value) {
        this(type, (Character)value);
    }

    public Value(FieldType.ArrayType type, java.lang.Object value) {
        // XXX: more type check?
        this((FieldType)type, value);
    }

    public Value(FieldType type, Object value) {
        this(type, (java.lang.Object)value);
    }

    public Value(FieldType.ArrayType type, Object[] value) {
        this(type, (java.lang.Object)value);
    }

    public Value(FieldType type, Value.Class value) {
        this(type, (java.lang.Object)value);
    }

    // This is deliberately made private to perform type checking between type and value.
    private Value(FieldType type, java.lang.Object value) {
        this.type = type;
        this.value = value;
    }

    public FieldType getType() {
        return type;
    }

    public java.lang.Object getValue() {
        return value;
    }

    // This should be used only when type conversion is needed. Indicator: "must be of type int"
    public int getIntValue() {
        if (type instanceof FieldType.Int) {
            return (int)value;
        } else if (type instanceof FieldType.Char) {
            return (int)((char)value);
        } else if (type instanceof FieldType.Boolean) {
            return (Boolean)value ? 1 : 0;
        } else {
            throw new RuntimeException("unexpected type used with getIntValue: " + type);
        }
    }

    // Value.Class representes an instance of a class insntace. It holds static field values.
    public static class Class {
        private final ClassFile classFile;
        private final Map<String, Value> fields;

        public Class(ClassFile classFile) {
            this.classFile = classFile;
            this.fields = new HashMap<>();
        }

        public ClassFile getClassFile() {
            return classFile;
        }

        public Value getField(String field) {
            return fields.get(field);
        }

        public void setField(String field, Value value) {
            fields.put(field, value);
        }
    }

    // Any non-primitive type uses Value.Object to represent an instance holding its field values.
    public static class Object {
        private final Map<String, Value> fields;

        public Object() {
            fields = new HashMap<>();
        }

        public Object(String str) {
            this();
            char[] value = new char[str.length()];
            str.getChars(0, str.length(), value, 0);
            setField("value", new Value(
                    new FieldType.ArrayType(new FieldType.Char()), value));
            // TODO: initializeObject of VM
            setField("hash", new Value(new FieldType.Int(), 0));
        }

        public Value getField(String field) {
            return fields.get(field);
        }

        public void setField(String field, Value value) {
            fields.put(field, value);
        }
    }
}
