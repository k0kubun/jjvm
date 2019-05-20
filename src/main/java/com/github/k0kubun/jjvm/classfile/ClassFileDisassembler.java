package com.github.k0kubun.jjvm.classfile;

import java.util.StringJoiner;

public class ClassFileDisassembler {
    private final ClassFile classfile;

    public ClassFileDisassembler(ClassFile classfile) {
        this.classfile = classfile;
    }

    public String disassemble() {
        StringJoiner flags = new StringJoiner(", ");
        classfile.getAccessFlags().stream().forEach(f -> flags.add(f.toString()));

        StringBuilder builder = new StringBuilder();
        builder.append(String.format("class %s\n", utf8Constant(classConstant(classfile.getThisClass()).getDescriptorIndex()).getString()));
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
            ConstantType type = classfile.getConstantPool()[i].getType();
            builder.append(String.format("%5s = %-19s", String.format("#%d", i + 1), type.toString()));

            if (type == ConstantType.Class) {
                int index = ((ConstantInfo.Class)classfile.getConstantPool()[i]).getDescriptorIndex();
                builder.append(String.format("#%-14d// %s", index, utf8Constant(index).getString()));
            } else if (type == ConstantType.Utf8) {
                builder.append(((ConstantInfo.Utf8)classfile.getConstantPool()[i]).getString());
            } else {
                builder.append("[TODO]");
            }

            builder.append("\n");
        }
        return builder.toString();
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
            System.out.println(method.getDescriptor().getParameters().size());

            StringJoiner flags = new StringJoiner(", ");
            method.getAccessFlags().stream().forEach(f -> flags.add(f.toString()));

            builder.append(String.format("\n  %s(%s);\n", declaration.toString(), args.toString()));
            builder.append(String.format("    descriptor: %s\n", method.getDescriptor().toString()));
            builder.append(String.format("    flags: %s\n", flags.toString()));

            for (AttributeInfo attribute : method.getAttributes()) {
                builder.append(disassembleAttribute(attribute, 2));
            }
        }
        builder.append("}\n");
        return builder.toString();
    }

    private String disassembleAttribute(AttributeInfo attribute, int indentLevel) {
        IndentedString builder = new IndentedString(indentLevel);
        if (attribute.getName().equals("Code")) {
            AttributeInfo.Code codeAttribute = (AttributeInfo.Code)attribute;
            builder.append(String.format("%s:\n", attribute.getName()));
            builder.append(String.format("  stack=%d, locals=%d\n", codeAttribute.getMaxStack(), codeAttribute.getMaxLocals()));
            int pos = 0;
            for (Instruction instruction : codeAttribute.getInstructions()) {
                builder.append(String.format("  %4d: %s\n", pos, instruction.getOpcode().getName()));
                pos += 1 + instruction.getOpcode().getArgc();
            }
            for (AttributeInfo attr : codeAttribute.getAttributes()) {
                builder.appendIndented(disassembleAttribute(attr, indentLevel + 1));
            }
        } else {
            builder.append(String.format("%s: [TODO]\n", attribute.getName()));
        }
        return builder.toString();
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
