package com.github.k0kubun.jjvm.classfile;

import java.util.StringJoiner;
import com.github.k0kubun.jjvm.classfile.Instruction.Opcode;

public class ClassFileDisassembler {
    private final ClassFile classfile;

    public ClassFileDisassembler(ClassFile classfile) {
        this.classfile = classfile;
    }

    public String disassemble() {
        StringJoiner flags = new StringJoiner(", ");
        classfile.getAccessFlags().stream().forEach(f -> flags.add(f.toString()));

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("class %s\n", utf8Constant(classConstant(classfile.getThisClass()).getNameIndex()).getString()));
        builder.append(String.format("  minor version: %d\n", classfile.getMinorVersion()));
        builder.append(String.format("  major version: %d\n", classfile.getMajorVersion()));
        builder.append(String.format("  flags: %s\n", flags.toString()));
        builder.append(disassembleConstantPool());
        builder.append(disassembleMethods());
        return builder.toString();
    }

    private String disassembleConstantPool() {
        StringBuilder builder = new StringBuilder();
        builder.append("Constant pool:\n");
        for (int i = 0; i < classfile.getConstantPool().length; i++) {
            ConstantInfo info = classfile.getConstantPool()[i];
            builder.append(String.format("%5s = %-19s", String.format("#%d", i + 1), info.getType().toString()));

            if (info instanceof ConstantInfo.NamedInfo) {
                int index = ((ConstantInfo.NamedInfo)info).getNameIndex();
                builder.append(String.format("#%-14d", index));
            } else if (info instanceof ConstantInfo.ClassRefInfo) {
                int classIndex = ((ConstantInfo.ClassRefInfo)info).getClassIndex();
                int nameAndTypeIndex = ((ConstantInfo.ClassRefInfo)info).getNameAndTypeIndex();
                builder.append(String.format("#%-14s",
                        String.format("%d.#%d", classIndex, nameAndTypeIndex)));
            } else if (info instanceof ConstantInfo.Utf8) {
                builder.append(((ConstantInfo.Utf8) classfile.getConstantPool()[i]).getString());
            } else if (info instanceof ConstantInfo.NameAndType) {
                builder.append(String.format("#%-14s// %s",
                        String.format("%d:#%d",
                                ((ConstantInfo.NameAndType)info).getNameIndex(),
                                ((ConstantInfo.NameAndType)info).getDescriptorIndex()),
                        getNameAndType(i + 1)));
            } else {
                builder.append("[TODO]");
            }

            String label = getConstantLabel(info);
            if (label != null) {
                builder.append("// ");
                builder.append(label);
            }

            builder.append("\n");
        }
        return builder.toString();
    }

    private String getConstantLabel(ConstantInfo info) {
        if (info instanceof ConstantInfo.NamedInfo) {
            int index = ((ConstantInfo.NamedInfo)info).getNameIndex();
            return utf8Constant(index).getString();
        } else if (info instanceof ConstantInfo.ClassRefInfo) {
            int classIndex = ((ConstantInfo.ClassRefInfo)info).getClassIndex();
            int nameAndTypeIndex = ((ConstantInfo.ClassRefInfo)info).getNameAndTypeIndex();
            return String.format("%s.%s",
                    utf8Constant(classConstant(classIndex).getNameIndex()).getString(),
                    getNameAndType(nameAndTypeIndex));
        } else {
            return null;
        }
    }

    private String disassembleMethods() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (MethodInfo method : classfile.getMethods()) {
            StringJoiner declaration = new StringJoiner(" ");
            method.getAccessFlags().stream().forEach(f -> declaration.add(f.getName()));
            declaration.add(method.getDescriptor().getReturn().getType());
            declaration.add(method.getName());

            StringJoiner args = new StringJoiner(", ");
            method.getDescriptor().getParameters().stream().forEach(p -> args.add(p.getType()));

            StringJoiner flags = new StringJoiner(", ");
            method.getAccessFlags().stream().forEach(f -> flags.add(f.toString()));

            builder.append(String.format("\n  %s(%s);\n", declaration.toString(), args.toString()));
            builder.append(String.format("    descriptor: %s\n", method.getDescriptor().toString()));
            builder.append(String.format("    flags: %s\n", flags.toString()));

            for (AttributeInfo attribute : method.getAttributes()) {
                builder.append(disassembleAttribute(attribute, method, 2));
            }
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String disassembleAttribute(AttributeInfo attribute, MethodInfo method, int indentLevel) {
        IndentedString builder = new IndentedString(indentLevel);
        if (attribute.getName().equals("Code")) {
            AttributeInfo.Code codeAttribute = (AttributeInfo.Code)attribute;
            int argsSize = method.getDescriptor().getParameters().size();
            if (!method.getAccessFlags().contains(MethodInfo.AccessFlag.ACC_STATIC))
                argsSize++;

            builder.append(String.format("%s:\n", attribute.getName()));
            builder.append(String.format("  stack=%d, locals=%d, args_size=%d\n", codeAttribute.getMaxStack(), codeAttribute.getMaxLocals(), argsSize));
            int pos = 0;
            for (Instruction instruction : codeAttribute.getInstructions()) {
                builder.append(String.format("  %4d: %s\n", pos, disassembleInstruction(instruction)));
                pos += 1 + instruction.getOpcode().getArgc();
            }
            for (AttributeInfo attr : codeAttribute.getAttributes()) {
                builder.appendIndented(disassembleAttribute(attr, method, indentLevel + 1));
            }
        } else {
            builder.append(String.format("%s: [TODO]\n", attribute.getName()));
        }
        return builder.toString();
    }

    private String disassembleInstruction(Instruction instruction) {
        String name = instruction.getOpcode().getName();
        Opcode opcode = instruction.getOpcode();

        if (opcode == Opcode.Ldc
                || opcode == Opcode.Getstatic
                || opcode == Opcode.Invokevirtual
                || opcode == Opcode.Invokespecial) {
            int index;
            if (opcode == Opcode.Ldc) {
                index = instruction.getOperands()[0];
            } else {
                index = instruction.getIndex();
            }
            ConstantInfo info = constant(index);

            String label = getConstantLabel(info);
            if (label == null)
                label = "[TODO]";
            return String.format("%-13s #%-19d// %s %s",
                    name,
                    index,
                    info.getType().toString().replaceFirst("ref\\z", ""),
                    label);
        } else {
            return name;
        }
    }

    private String getNameAndType(int index) {
        ConstantInfo.NameAndType info = (ConstantInfo.NameAndType)constant(index);
        return String.format("%s:%s",
                utf8Constant(info.getNameIndex()).getString(),
                utf8Constant(info.getDescriptorIndex()).getString());
    }

    private ConstantInfo.Class classConstant(int index) {
        return (ConstantInfo.Class)constant(index);
    }

    private ConstantInfo.Utf8 utf8Constant(int index) {
        return (ConstantInfo.Utf8)constant(index);
    }

    private ConstantInfo constant(int index) {
        return classfile.getConstantPool()[index - 1];
    }

    private static class IndentedString {
        private final int indentLevel;
        private final StringBuilder builder;

        public IndentedString(int indentLevel) {
            this.indentLevel = indentLevel;
            this.builder = new StringBuilder();
        }

        public void append(String string) {
            for (int i = 0; i < indentLevel; i++) {
                builder.append("  ");
            }
            appendIndented(string);
        }

        public void appendIndented(String string) {
            builder.append(string);
        }

        public String toString() {
            return builder.toString();
        }
    }
}
