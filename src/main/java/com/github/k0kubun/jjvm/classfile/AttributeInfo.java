package com.github.k0kubun.jjvm.classfile;

public class AttributeInfo {
    private final String name;

    public AttributeInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class Code extends AttributeInfo {
        private final int maxStack;
        private final int maxLocals;
        private final Opcode[] opcodes;
        private final ExceptionTableEntry[] exceptionTable;
        private final AttributeInfo[] attributes;

        public Code(int maxStack, int maxLocals, Opcode[] opcodes, ExceptionTableEntry[] exceptionTable, AttributeInfo[] attributes) {
            super("Code");
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
            this.opcodes = opcodes;
            this.exceptionTable = exceptionTable;
            this.attributes = attributes;
        }

        public Opcode[] getOpcodes() {
            return opcodes;
        }

        public int getMaxStack() {
            return maxStack;
        }

        public int getMaxLocals() {
            return maxLocals;
        }

        public AttributeInfo[] getAttributes() {
            return attributes;
        }

        public static class ExceptionTableEntry {
            private final int startPc;
            private final int endPc;
            private final int handlerPc;
            private final int catchType;

            public ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {
                this.startPc = startPc;
                this.endPc = endPc;
                this.handlerPc = handlerPc;
                this.catchType = catchType;
            }
        }
    }
}
