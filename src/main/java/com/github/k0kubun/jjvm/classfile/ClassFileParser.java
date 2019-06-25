package com.github.k0kubun.jjvm.classfile;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
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
        resolveConstantPoolReferences(constantPool);
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

        if (stream.read() != -1) {
            throw new RuntimeException(String.format("classfile did not reach EOF after parseField (available: %d)", stream.available()));
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
                byte[] bytes = new byte[4];
                for (int j = 0; j < 4; j++) {
                    bytes[j] = stream.readByte();
                }
                info = new ConstantInfo.Float(bytes);
            } else if (type == ConstantType.Long) {
                // CONSTANT_Long_info {
                //     u1 tag;
                //     u4 high_bytes;
                //     u4 low_bytes;
                // }
                info = new ConstantInfo.Long(stream.readLong());
            } else if (type == ConstantType.Double) {
                // CONSTANT_Double_info {
                //     u1 tag;
                //     u4 high_bytes;
                //     u4 low_bytes;
                // }
                byte[] bytes = new byte[8];
                for (int j = 0; j < 8; j++) {
                    bytes[j] = stream.readByte();
                }
                info = new ConstantInfo.Double(bytes);
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
                info = new ConstantInfo.MethodType(stream.readUnsignedShort());
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

            if (type == ConstantType.Long || type == ConstantType.Double) {
                // All 8-byte constants take up two entries in the constant_pool table of the class file.
                i++;
            }
        }
        return constantPool;
    }

    // Resolve reference to constantPool by index in constants
    private void resolveConstantPoolReferences(ConstantInfo[] constantPool) {
        for (ConstantInfo constant : constantPool) {
            if (constant instanceof ConstantInfo.Class) {
                ConstantInfo.Class info = (ConstantInfo.Class)constant;
                info.setName(((ConstantInfo.Utf8)constantPool[info.getNameIndex() - 1]).getString());
            } else if (constant instanceof ConstantInfo.Fieldref) {
                ConstantInfo.Fieldref info = (ConstantInfo.Fieldref)constant;
                info.setClassInfo((ConstantInfo.Class)constantPool[info.getClassIndex() - 1]);
                info.setNameAndType((ConstantInfo.NameAndType)constantPool[info.getNameAndTypeIndex() - 1]);
            } else if (constant instanceof ConstantInfo.Methodref) {
                ConstantInfo.Methodref info = (ConstantInfo.Methodref)constant;
                info.setClassInfo((ConstantInfo.Class)constantPool[info.getClassIndex() - 1]);
                info.setNameAndType((ConstantInfo.NameAndType)constantPool[info.getNameAndTypeIndex() - 1]);
            } else if (constant instanceof ConstantInfo.InterfaceMethodref) {
                ConstantInfo.InterfaceMethodref info = (ConstantInfo.InterfaceMethodref)constant;
                info.setClassInfo((ConstantInfo.Class)constantPool[info.getClassIndex() - 1]);
                info.setNameAndType((ConstantInfo.NameAndType)constantPool[info.getNameAndTypeIndex() - 1]);
            } else if (constant instanceof ConstantInfo.String) {
                ConstantInfo.String info = (ConstantInfo.String)constant;
                info.setString(((ConstantInfo.Utf8)constantPool[info.getStringIndex() - 1]).getString());
            } else if (constant instanceof ConstantInfo.Integer
                    || constant instanceof ConstantInfo.Float
                    || constant instanceof ConstantInfo.Long
                    || constant instanceof ConstantInfo.Double) {
                // not needed
            } else if (constant instanceof ConstantInfo.NameAndType) {
                ConstantInfo.NameAndType info = (ConstantInfo.NameAndType)constant;
                info.setName(((ConstantInfo.Utf8)constantPool[info.getNameIndex() - 1]).getString());
                info.setDescriptor(((ConstantInfo.Utf8)constantPool[info.getDescriptorIndex() - 1]).getString());
            } else if (constant instanceof ConstantInfo.Utf8) {
                // not needed
            } else if (constant instanceof ConstantInfo.MethodHandle
                    || constant instanceof ConstantInfo.MethodType
                    || constant instanceof ConstantInfo.InvokeDynamic) {
                // not used yet
            } else if (constant != null) { // Long/Double may leave create a blank space
                throw new UnsupportedOperationException("Unhandled ConstantType: " + constant);
            }
        }
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
            fields[i] = new FieldInfo(accessFlags, nameIndex, descriptorIndex, attributes, constantPool);
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
            String name = ((ConstantInfo.Utf8)constantPool[nameIndex - 1]).getString();
            AttributeInfo[] attributes = parseAttributes(stream, attributesCount, constantPool);
            MethodInfo.Descriptor descriptor = DescriptorParser.parseMethod(getString(constantPool, descriptorIndex));
            methods[i] = new MethodInfo(accessFlags, getString(constantPool, nameIndex), descriptor, attributes);
            if (name == null) {
                throw new RuntimeException(name);
            }
        }
        return methods;
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
            } else if (attributeName.equals("LineNumberTable")) {
                attributes[j] = parseLineNumberTableAttribute(stream);
            } else if (attributeName.equals("StackMapTable")) {
                attributes[j] = parseStackMapTableAttribute(stream);
            } else if (attributeName.equals("SourceFile")) {
                attributes[j] = parseSourceFileAttribute(stream);
            } else if (attributeName.equals("ConstantValue")) {
                attributes[j] = parseConstantValueAttribute(stream, constantPool);
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
        Instruction[] code = parseCode(stream, stream.readInt());

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

    private Instruction[] parseCode(DataInputStream inputStream, int codeLength) throws IOException {
        CountedInputStream stream = new CountedInputStream(inputStream);
        Instruction[] instructions = new Instruction[codeLength];
        for (int i = 0; i < codeLength;) {
            byte code = (byte)stream.readUnsignedByte();
            Instruction.Opcode opcode = Instruction.Opcode.fromCode(code);

            byte[] operands;
            int argc = opcode.getArgc();
            int padSize = 0;
            if (argc == -1) { // variable-length
                int mod4 = stream.getCounter() % 4;
                padSize = (mod4 == 0 ? 0 : 4 - mod4);
                byte[] pad = new byte[padSize];
                byte[] defaultByte = new byte[4];
                stream.read(pad);
                stream.read(defaultByte);

                switch (opcode) {
                    case Tableswitch:
                        byte[] low = new byte[4];
                        byte[] high = new byte[4];
                        stream.read(low);
                        stream.read(high);
                        byte[] jumpOffsets = new byte[4 * (ByteBuffer.wrap(high).getInt() - ByteBuffer.wrap(low).getInt() + 1)];
                        stream.read(jumpOffsets);

                        operands = concatByteArrays(pad, defaultByte, low, high, jumpOffsets);
                        break;
                    case Lookupswitch:
                        byte[] nPairs = new byte[4];
                        stream.read(nPairs);
                        byte[] pairs = new byte[8 * ByteBuffer.wrap(nPairs).getInt()];
                        stream.read(pairs);

                        operands = concatByteArrays(pad, defaultByte, nPairs, pairs);
                        break;
                    default:
                        throw new RuntimeException("unexpected variable-length opcode: " + opcode.getName());
                }
            } else {
                operands = new byte[argc];
                for (int j = 0; j < argc; j++) {
                    operands[j] = (byte)stream.readUnsignedByte();
                }
            }

            instructions[i] = new Instruction(opcode, operands, padSize);
            i += 1 + operands.length;
        }
        return instructions;
    }

    private byte[] concatByteArrays(byte[]... arrays) {
        int length = 0;
        for (byte[] array : arrays) {
            length += array.length;
        }

        ByteBuffer buffer = ByteBuffer.allocate(length);
        for (byte[] array : arrays) {
            buffer.put(array);
        }
        return buffer.array();
    }

    // LineNumberTable_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 line_number_table_length;
    //     {   u2 start_pc;
    //         u2 line_number;
    //     } line_number_table[line_number_table_length];
    // }
    private AttributeInfo.LineNumberTable parseLineNumberTableAttribute(DataInputStream stream) throws IOException {
        int tableLength = stream.readUnsignedShort();
        AttributeInfo.LineNumberTable.LineNumberEntry[] table = new AttributeInfo.LineNumberTable.LineNumberEntry[tableLength];

        for (int i = 0; i < tableLength; i++) {
            int startPc = stream.readUnsignedShort();
            int lineNumber = stream.readUnsignedShort();
            table[i] = new AttributeInfo.LineNumberTable.LineNumberEntry(startPc, lineNumber);
        }
        return new AttributeInfo.LineNumberTable(table);
    }

    // StackMapTable_attribute {
    //     u2              attribute_name_index;
    //     u4              attribute_length;
    //     u2              number_of_entries;
    //     stack_map_frame entries[number_of_entries];
    // }
    private AttributeInfo.StackMapTable parseStackMapTableAttribute(DataInputStream stream) throws IOException {
        int numberOfEntries = stream.readUnsignedShort();
        AttributeInfo.StackMapTable.StackMapFrame[] entries = new AttributeInfo.StackMapTable.StackMapFrame[numberOfEntries];
        for (int i = 0; i < numberOfEntries; i++) {
            int tag = stream.readUnsignedByte();
            if (0 <= tag && tag <= 63) {
                // same_frame {
                //     u1 frame_type = SAME; /* 0-63 */
                // }
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.Same(tag);
            } else if (64 <= tag && tag <= 127) {
                // same_locals_1_stack_item_frame {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM; /* 64-127 */
                //     verification_type_info stack[1];
                // }
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.SameLocals1StackItem(tag,
                        parseVerificationTypeInfo(stream, 1));
            } else if (tag == 247) {
                // same_locals_1_stack_item_frame_extended {
                //     u1 frame_type = SAME_LOCALS_1_STACK_ITEM_EXTENDED; /* 247 */
                //     u2 offset_delta;
                //     verification_type_info stack[1];
                // }
                int offsetDelta = stream.readUnsignedShort();
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.SameLocals1StackItemExtended(tag, offsetDelta,
                        parseVerificationTypeInfo(stream, 1));
            } else if (248 <= tag && tag <= 250) {
                // chop_frame {
                //     u1 frame_type = CHOP; /* 248-250 */
                //     u2 offset_delta;
                // }
                int offsetDelta = stream.readUnsignedShort();
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.Chop(tag, offsetDelta);
            } else if (tag == 251) {
                // same_frame_extended {
                //     u1 frame_type = SAME_FRAME_EXTENDED; /* 251 */
                //     u2 offset_delta;
                // }
                int offsetDelta = stream.readUnsignedShort();
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.SameFrameExtended(tag, offsetDelta);
            } else if (252 <= tag && tag <= 254) {
                // append_frame {
                //     u1 frame_type = APPEND; /* 252-254 */
                //     u2 offset_delta;
                //     verification_type_info locals[frame_type - 251];
                // }
                int offsetDelta = stream.readUnsignedShort();
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.Append(tag, offsetDelta,
                        parseVerificationTypeInfo(stream, tag - 251));
            } else if (tag == 255) {
                // full_frame {
                //     u1 frame_type = FULL_FRAME; /* 255 */
                //     u2 offset_delta;
                //     u2 number_of_locals;
                //     verification_type_info locals[number_of_locals];
                //     u2 number_of_stack_items;
                //     verification_type_info stack[number_of_stack_items];
                // }
                int offsetDelta = stream.readUnsignedShort();
                int numberOfLocals = stream.readUnsignedShort();
                AttributeInfo.StackMapTable.VerificationTypeInfo[] locals = parseVerificationTypeInfo(stream, numberOfLocals);
                int numberOfStackItems = stream.readUnsignedShort();
                AttributeInfo.StackMapTable.VerificationTypeInfo[] stack = parseVerificationTypeInfo(stream, numberOfStackItems);
                entries[i] = new AttributeInfo.StackMapTable.StackMapFrame.FullFrame(tag, offsetDelta, locals, stack);
            } else {
                throw new RuntimeException("Unexpected tag for StackMapFrame: " + tag);
            }
        }
        return new AttributeInfo.StackMapTable(entries);
    }

    private AttributeInfo.StackMapTable.VerificationTypeInfo[] parseVerificationTypeInfo(DataInputStream stream, int n) throws IOException {
        AttributeInfo.StackMapTable.VerificationTypeInfo[] infos = new AttributeInfo.StackMapTable.VerificationTypeInfo[n];
        for (int i = 0; i < n; i++) {
            int tag = stream.readUnsignedByte();
            // TODO: bake tag type to VerificationTypeInfo
            if (tag == 7) {
                stream.readUnsignedShort(); // cpool_index
            } else if (tag == 8) {
                stream.readUnsignedShort(); // offset
            }
            infos[i] = new AttributeInfo.StackMapTable.VerificationTypeInfo(tag);
        }
        return infos;
    }

    // SourceFile_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 sourcefile_index;
    // }
    private AttributeInfo.SourceFile parseSourceFileAttribute(DataInputStream stream) throws IOException {
        int index = stream.readUnsignedShort();
        return new AttributeInfo.SourceFile(index);
    }

    // ConstantValue_attribute {
    //     u2 attribute_name_index;
    //     u4 attribute_length;
    //     u2 constantvalue_index;
    // }
    private AttributeInfo.ConstantValue parseConstantValueAttribute(DataInputStream stream, ConstantInfo[] constantPool) throws IOException {
        int index = stream.readUnsignedShort();
        return new AttributeInfo.ConstantValue(constantPool[index - 1]);
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

    public static class DescriptorParser {
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
        public static MethodInfo.Descriptor parseMethod(String descriptor) {
            StringScanner scanner = new StringScanner(descriptor);
            if (scanner.nextChar() != '(') {
                throw new RuntimeException(String.format("method descriptor should start with '(', but was: %s", descriptor));
            }

            List<FieldType> parameters = new ArrayList<>();
            while (scanner.peekChar() != ')') {
                parameters.add(DescriptorParser.parseField(scanner));
            }
            scanner.nextChar(); // )

            MethodInfo.ReturnDescriptor returnDescriptor = scanner.peekChar() != 'V' ?
                    parseField(scanner) : new MethodInfo.VoidDescriptor();
            return new MethodInfo.Descriptor(descriptor, returnDescriptor, parameters);
        }

        public static FieldType parseField(String descriptor) {
            StringScanner scanner = new StringScanner(descriptor);
            return parseField(scanner);
        }

        public static FieldType parseField(StringScanner scanner) {
            return scanFieldType(scanner);
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
        private static FieldType scanFieldType(StringScanner scanner) {
            char c = scanner.nextChar();
            switch (c) {
                case 'B':
                    return new FieldType.Byte();
                case 'C':
                    return new FieldType.Char();
                case 'D':
                    return new FieldType.Double();
                case 'F':
                    return new FieldType.Float();
                case 'I':
                    return new FieldType.Int();
                case 'J':
                    return new FieldType.Long();
                case 'S':
                    return new FieldType.Short();
                case 'Z':
                    return new FieldType.Boolean();
                case 'L':
                    String className = scanner.scanUntil(';');
                    return new FieldType.ObjectType(className.substring(0, className.length() - 1));
                case '[':
                    return new FieldType.ArrayType(scanFieldType(scanner));
                default:
                    throw new UnsupportedOperationException(String.format("unexpected FieldType: %c", c));
            }
        }
    }

    private static class StringScanner {
        private final String string;
        private int pos;

        StringScanner(String string) {
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

    // A wrapper of DataInputStream that allows to count how many bytes are read.
    private static class CountedInputStream {
        private final DataInputStream stream;
        private int counter;

        CountedInputStream(DataInputStream in) {
            stream = in;
            counter = 0;
        }

        public int getCounter() {
            return counter;
        }

        public int readUnsignedByte() throws IOException {
            counter++;
            return stream.readUnsignedByte();
        }

        public void read(byte[] buf) throws IOException {
            counter += buf.length;
            stream.read(buf);
        }
    }
}
