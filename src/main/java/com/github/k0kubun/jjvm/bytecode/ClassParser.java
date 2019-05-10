package com.github.k0kubun.jjvm.bytecode;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;

/* https://docs.oracle.com/javase/specs/jvms/se11/html/jvms-4.html
ClassFile {
    u4             magic;
    u2             minor_version;
    u2             major_version;
    u2             constant_pool_count;
    cp_info        constant_pool[constant_pool_count-1];
    u2             access_flags;
    u2             this_class;
    u2             super_class;
    u2             interfaces_count;
    u2             interfaces[interfaces_count];
    u2             fields_count;
    field_info     fields[fields_count];
    u2             methods_count;
    method_info    methods[methods_count];
    u2             attributes_count;
    attribute_info attributes[attributes_count];
} */
public class ClassParser {
    public ClassFile parseClassFile(String filename) throws IOException {
        DataInputStream stream = new DataInputStream(new FileInputStream(filename));

        Integer magic = stream.readInt();
        Integer minorVersion = stream.readUnsignedShort();
        Integer majorVersion = stream.readUnsignedShort();
        Integer constantPoolCount = stream.readUnsignedShort();

        return new ClassFile(
                magic,
                minorVersion,
                majorVersion,
                constantPoolCount
        );
    }
}
