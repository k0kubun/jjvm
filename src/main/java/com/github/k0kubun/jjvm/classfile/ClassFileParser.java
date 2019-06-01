package com.github.k0kubun.jjvm.classfile;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html
public class ClassFileParser {
    // ClassFile {
    //     u4             magic;
    //     u2             minor_version;
    //     u2             major_version;
    //     u2             constant_pool_count;
    //     cp_info        constant_pool[constant_pool_count-1];
    //     u2             access_flags;
    //     u2             this_class;
    //     u2             super_class;
    //     u2             interfaces_count;
    //     u2             interfaces[interfaces_count];
    //     u2             fields_count;
    //     field_info     fields[fields_count];
    //     u2             methods_count;
    //     method_info    methods[methods_count];
    //     u2             attributes_count;
    //     attribute_info attributes[attributes_count];
    // }
    public ClassFile parse(InputStream inputStream) throws IOException {
        DataInputStream stream = new DataInputStream(inputStream);

        int magic = stream.readInt();
        int minorVersion = stream.readUnsignedShort();
        int majorVersion = stream.readUnsignedShort();
        int constantPoolCount = stream.readUnsignedShort();
        ConstantInfo[] constantPool = parseConstantPool(stream, constantPoolCount - 1);
        int accessFlags = stream.readUnsignedShort();
        int thisClass = stream.readUnsignedShort();
        int superClass = stream.readUnsignedShort();
        int interfacesCount = stream.readUnsignedShort();
        int[] interfaces = readUnsignedShorts(stream, interfacesCount);
        int fieldsCount = stream.readUnsignedShort();
        FieldInfo[] fields = parseFields(stream, fieldsCount, constantPool);
        int methodsCount = stream.readUnsignedShort();
        MethodInfo[] methods = parseMethods(stream, methodsCount, constantPool);
        int attributesCount = stream.readUnsignedShort();
        AttributeInfo[] attributes = parseAttributes(stream, attributesCount, constantPool);

        if (stream.available() > 0) {
            throw new RuntimeException(String.format("classfile did not reach EOF after parse (available: %d)", stream.available()));
        }

        return new ClassFile(
                magic,
                minorVersion,
                majorVersion,
                constantPool,
                accessFlags,
                thisClass,
                superClass,
                interfaces,
                fields,
                methods,
                attributes
        );
    }

    public ClassFile parse(String filename) throws IOException {
        return parse(new FileInputStream(filename));
    }

    // cp_info {
    //     u1 tag;
    //     u1 info[];
    // }
    private ConstantInfo[] parseConstantPool(DataInputStream stream, int constantPoolCount) throws IOException {
        ConstantInfo[] constantPool = new ConstantInfo[constantPoolCount];
        for (int i = 0; i < constantPoolCount; i++) {
            int tag = stream.readUnsignedByte();
            ConstantType type = ConstantType.fromTag(tag);

            ConstantInfo info;
            if (type == ConstantType.Class) {
                // CONSTANT_Class_info {
                //     u1 tag;
                //     u2 name_index;
                // }
                info = new ConstantInfo.Class(stream.readUnsignedShort());
            } else if (type == ConstantType.Fieldref) {
                // CONSTANT_Fieldref_info {
                //     u1 tag;
                //     u2 class_index;
                //     u2 name_and_type_index;
                // }
                info = new ConstantInfo.Fieldref(stream.readUnsignedShort(), stream.readUnsignedShort());
            } else if (type == ConstantType.Methodref) {
                // CONSTANT_Methodref_info {
                //     u1 tag;
                //     u2 class_index;
                //     u2 name_and_type_index;
                // }
                info = new ConstantInfo.Methodref(stream.readUnsignedShort(), stream.readUnsignedShort());
            } else if (type == ConstantType.InterfaceMethodref) {
                // CONSTANT_InterfaceMethodref_info {
                //     u1 tag;
                //     u2 class_index;
                //     u2 name_and_type_index;
                // }
                info = new ConstantInfo.InterfaceMethodref(stream.readUnsignedShort(), stream.readUnsignedShort());
            } else if (type == ConstantType.String) {
                // CONSTANT_String_info {
                //     u1 tag;
                //     u2 string_index;
                // }
                info = new ConstantInfo.String(stream.readUnsignedShort());
            } else if (type == ConstantType.Integer) {
                // CONSTANT_Integer_info {
                //     u1 tag;
                //     u4 bytes;
                // }
                info = new ConstantInfo.Integer(stream.readInt());
            } else if (type == ConstantType.Float) {
                // CONSTANT_Float_info {
                //     u1 tag;
                //     u4 bytes;
                // }
                info = new ConstantInfo.Float(stream.readInt());
            } else if (type == ConstantType.Long) {
                // CONSTANT_Long_info {
                //     u1 tag;
                //     u4 high_bytes;
                //     u4 low_bytes;
                // }
                info = new ConstantInfo.Long(stream.readInt(), stream.readInt());
            } else if (type == ConstantType.Double) {
                // CONSTANT_Double_info {
                //     u1 tag;
                //     u4 high_bytes;
                //     u4 low_bytes;
                // }
                info = new ConstantInfo.Double(stream.readInt(), stream.readInt());
            } else if (type == ConstantType.NameAndType) {
                // CONSTANT_NameAndType_info {
                //     u1 tag;
                //     u2 name_index;
                //     u2 descriptor_index;
                // }
                info = new ConstantInfo.NameAndType(stream.readUnsignedShort(), stream.readUnsignedShort());
            } else if (type == ConstantType.Utf8) {
                // CONSTANT_Utf8_info {
                //     u1 tag;
                //     u2 length;
                //     u1 bytes[length];
                // }
                byte[] bytes = new byte[stream.readUnsignedShort()];
                stream.read(bytes);
                info = new ConstantInfo.Utf8(bytes);
            } else if (type == ConstantType.MethodHandle) {
                // CONSTANT_MethodHandle_info {
                //     u1 tag;
                //     u1 reference_kind;
                //     u2 reference_index;
                // }
                info = new ConstantInfo.MethodHandle(stream.readUnsignedByte(), stream.readUnsignedShort());
            } else if (type == ConstantType.MethodType) {
                // CONSTANT_MethodType_info {
                //     u1 tag;
                //     u2 descriptor_index;
                // }
                info = new ConstantInfo.MethodType(stream.readUnsignedByte());
            } else if (type == ConstantType.InvokeDynamic) {
                // CONSTANT_InvokeDynamic_info {
                //     u1 tag;
                //     u2 bootstrap_method_attr_index;
                //     u2 name_and_type_index;
                // }
                info = new ConstantInfo.InvokeDynamic(stream.readUnsignedShort(), stream.readUnsignedShort());
            } else {
                throw new UnsupportedOperationException(String.format("Unhandled ConstantType (tag:%d)", tag));
            }
            constantPool[i] = info;
        }
        return constantPool;
    }

    // field_info {
    //     u2             access_flags;
    //     u2             name_index;
    //     u2             descriptor_index;
    //     u2             attributes_count;
    //     attribute_info attributes[attributes_count];
    // }
    private FieldInfo[] parseFields(DataInputStream stream, int fieldsCount, ConstantInfo[] constantPool) throws IOException {
        FieldInfo[] fields = new FieldInfo[fieldsCount];
        for (int i = 0; i < fieldsCount; i++) {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            int descriptorIndex = stream.readUnsignedShort();
            int attributesCount = stream.readUnsignedShort();
            AttributeInfo[] attributes = parseAttributes(stream, attributesCount, constantPool);
            fields[i] = new FieldInfo(accessFlags, nameIndex, descriptorIndex, attributes);
        }
        return fields;
    }

    // method_info {
    //     u2             access_flags;
    //     u2             name_index;
    //     u2             descriptor_index;
    //     u2             attributes_count;
    //     attribute_info attributes[attributes_count];
    // }
    private MethodInfo[] parseMethods(DataInputStream stream, int methodsCount, ConstantInfo[] constantPool) throws IOException {
        MethodInfo[] methods = new MethodInfo[methodsCount];
        for (int i = 0; i < methodsCount; i++) {
            int accessFlags = stream.readUnsignedShort();
            int nameIndex = stream.readUnsignedShort();
            int descriptorIndex = stream.readUnsignedShort();
            int attributesCount = stream.readUnsignedShort();
            AttributeInfo[] attributes = parseAttributes(stream, attributesCount, constantPool);
            MethodInfo.Descriptor descriptor = parseMethodDescriptor(getString(constantPool, descriptorIndex));
            methods[i] = new MethodInfo(accessFlags, getString(constantPool, nameIndex), descriptor, attributes);
        }
        return methods;
    }

    // MethodDescriptor:
    //   ( {ParameterDescriptor} ) ReturnDescriptor
    //
    // ParameterDescriptor:
    //   FieldType
    //
    // ReturnDescriptor:
    //   FieldType
    //   VoidDescriptor
    //
    // VoidDescriptor:
    //   V
    private MethodInfo.Descriptor parseMethodDescriptor(String descriptor) {
        StringScanner scanner = new StringScanner(descriptor);
        if (scanner.nextChar() != '(') {
            throw new RuntimeException(String.format("method descriptor should start with '(', but was: %s", descriptor));
        }

        List<FieldType> parameters = new ArrayList<>();
        while (scanner.peekChar() != ')') {
            parameters.add(scanFieldType(scanner));
        }
        scanner.nextChar(); // )

        MethodInfo.ReturnDescriptor returnDescriptor = scanner.peekChar() != 'V' ?
                scanFieldType(scanner) : new MethodInfo.VoidDescriptor();
        return new MethodInfo.Descriptor(descriptor, returnDescriptor, parameters);
    }

    // FieldDescriptor:
    //   FieldType
    //
    // FieldType:
    //   BaseType
    //   ObjectType
    //   ArrayType
    //
    // BaseType:
    //   (one of)
    //   B C D F I J S Z
    //
    // ObjectType:
    //   L ClassName ;
    //
    // ArrayType:
    //   [ ComponentType
    //
    // ComponentType:
    //   FieldType
    private FieldType scanFieldType(StringScanner scanner) {
        char c = scanner.nextChar();
        switch (c) {
            case '[':
                return new FieldType.ArrayType(scanFieldType(scanner));
            case 'L':
                String className = scanner.scanUntil(';');
                return new FieldType.ObjectType(className.substring(0, className.length() - 1));
            default:
                throw new UnsupportedOperationException(String.format("unexpected FieldType: %c", c));
        }
    }

    // attribute_info {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u1 info[attribute_length];
    // }
    private AttributeInfo[] parseAttributes(DataInputStream stream, int attributesCount, ConstantInfo[] constantPool) throws IOException {
        AttributeInfo[] attributes = new AttributeInfo[attributesCount];
        for (int j = 0; j < attributesCount; j++) {
            int attributeNameIndex = stream.readUnsignedShort();
            int attributeLength = stream.readInt();

            String attributeName = getString(constantPool, attributeNameIndex);
            if (attributeName.equals("Code")) {
                attributes[j] = parseCodeAttribute(stream, constantPool);
            } else {
                stream.skipBytes(attributeLength);
                attributes[j] = new AttributeInfo(attributeName);
            }
        }
        return attributes;
    }

    // Code_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 max_stack;
    //     u2 max_locals;
    //     u4 code_length;
    //     u1 code[code_length];
    //     u2 exception_table_length;
    //     {   u2 start_pc;
    //         u2 end_pc;
    //         u2 handler_pc;
    //         u2 catch_type;
    //     } exception_table[exception_table_length];
    //     u2 attributes_count;
    //     attribute_info attributes[attributes_count];
    // }
    private AttributeInfo.Code parseCodeAttribute(DataInputStream stream, ConstantInfo[] constantPool) throws IOException {
        int maxStack = stream.readUnsignedShort();
        int maxLocals = stream.readUnsignedShort();
        List<Instruction> code = parseCode(stream, stream.readInt());

        int exceptionTableLength = stream.readUnsignedShort();
        AttributeInfo.Code.ExceptionTableEntry[] exceptionTable = new AttributeInfo.Code.ExceptionTableEntry[exceptionTableLength];
        for (int i = 0; i < exceptionTableLength; i++) {
            int startPc = stream.readUnsignedShort();
            int endPc = stream.readUnsignedShort();
            int handlerPc = stream.readUnsignedShort();
            int catchType = stream.readUnsignedShort();
            exceptionTable[i] = new AttributeInfo.Code.ExceptionTableEntry(startPc, endPc, handlerPc, catchType);
        }

        int attributesCount = stream.readUnsignedShort();
        AttributeInfo[] attributes = parseAttributes(stream, attributesCount, constantPool);

        return new AttributeInfo.Code(maxStack, maxLocals, code, exceptionTable, attributes);
    }

    private List<Instruction> parseCode(DataInputStream stream, int codeLength) throws IOException {
        List<Instruction> instructions = new ArrayList<>();
        int bytesRead = 0;
        while (bytesRead < codeLength) {
            byte code = (byte)stream.readUnsignedByte();
            Instruction.Opcode opcode = Instruction.Opcode.fromCode(code);

            byte[] operands = new byte[opcode.getArgc()];
            for (int i = 0; i < opcode.getArgc(); i++) {
                operands[i] = (byte)stream.readUnsignedByte();
            }

            instructions.add(new Instruction(opcode, operands));
            bytesRead += 1 + opcode.getArgc();
        }
        return instructions;
    }

    private int[] readUnsignedShorts(DataInputStream stream, int length) throws IOException {
        int[] shorts = new int[length];
        for (int i = 0; i < length; i++) {
            shorts[i] = stream.readUnsignedShort();
        }
        return shorts;
    }

    private String getString(ConstantInfo[] constantPool, int index) {
        return ((ConstantInfo.Utf8)constantPool[index - 1]).getString();
    }

    private static class StringScanner {
        private final String string;
        private int pos;

        public StringScanner(String string) {
            this.string = string;
            this.pos = 0;
        }

        public char peekChar() {
            return string.charAt(pos);
        }

        public char nextChar() {
            char ch = peekChar();
            pos++;
            return ch;
        }

        public String scanUntil(char character) {
            int index = string.indexOf(character, pos);
            String scanned = string.substring(pos, index + 1);
            pos += (index + 1 - pos);
            return scanned;
        }
    }
}
