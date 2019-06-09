package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ConstantInfo;
import com.github.k0kubun.jjvm.classfile.FieldType;

import java.util.HashMap;
import java.util.Map;

// A container of any JVM value. The contained value may NOT be an object.
public class Value {
    private final FieldType type;

    // `value` should have a value serialized depending on the `type`. The caller of
    // getValue() is responsible for deserializing the `value` depending on the `type`.
    //   I                   => Integer
    //   Ljava/lang/Integer; => Integer
    //   Ljava/lang/String;  => String
    //   [Ljava/lang/String; => String[]
    //   Ljava/lang/Class;   => Value.Class
    //   Lfoo/bar;           => Value.Object
    //   null (of any class) => null
    private final java.lang.Object value;

    public Value(FieldType type, java.lang.Object value) {
        this.type = type;
        this.value = value;
    }

    public FieldType getType() {
        return type;
    }

    public java.lang.Object getValue() {
        return value;
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

    // A non-native class (native: Integer, String, ...) uses Value.Object to represent
    // an instance holding its field values.
    // XXX: The native classes may need to be wrapped by Value.Object to have fields later.
    public static class Object {
        private final Map<String, Value> fields;

        public Object() {
            fields = new HashMap<>();
        }
    }
}
