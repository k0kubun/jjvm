package com.github.k0kubun.jjvm.classfile;

import java.util.List;

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
        private final List<Instruction> instructions;
        private final ExceptionTableEntry[] exceptionTable;
        private final AttributeInfo[] attributes;

        public Code(int maxStack, int maxLocals, List<Instruction> instructions, ExceptionTableEntry[] exceptionTable, AttributeInfo[] attributes) {
            super("Code");
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
            this.instructions = instructions;
            this.exceptionTable = exceptionTable;
            this.attributes = attributes;
        }

        public List<Instruction> getInstructions() {
            return instructions;
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

        public ExceptionTableEntry[] getExceptionTable() {
            return exceptionTable;
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

            public int getStartPc() {
                return startPc;
            }

            public int getEndPc() {
                return endPc;
            }

            public int getHandlerPc() {
                return handlerPc;
            }

            public int getCatchType() {
                return catchType;
            }
        }
    }

    public static class LineNumberTable extends AttributeInfo {
        private final LineNumberEntry[] lineNumberTable;

        public LineNumberTable(LineNumberEntry[] lineNumberTable) {
            super("LineNumberTable");
            this.lineNumberTable = lineNumberTable;
        }

        public LineNumberEntry[] getLineNumberTable() {
            return lineNumberTable;
        }

        public static class LineNumberEntry {
            private final int startPc;
            private final int lineNumber;

            public LineNumberEntry(int startPc, int lineNumber) {
                this.startPc = startPc;
                this.lineNumber = lineNumber;
            }

            public int getStartPc() {
                return startPc;
            }

            public int getLineNumber() {
                return lineNumber;
            }
        }
    }

    public static class StackMapTable extends AttributeInfo {
        private final StackMapFrame[] entries;

        public StackMapTable(StackMapFrame[] entries) {
            super("StackMapTable");
            this.entries = entries;
        }

        public static class StackMapFrame {
            private final int tag;

            public StackMapFrame(int tag) {
                this.tag = tag;
            }

            // 0-63
            public static class Same extends StackMapFrame {
                public Same(int tag) {
                    super(tag);
                }
            }

            // 64-127
            public static class SameLocals1StackItem extends StackMapFrame {
                private final VerificationTypeInfo[] stack;

                public SameLocals1StackItem(int tag, VerificationTypeInfo[] stack) {
                    super(tag);
                    this.stack = stack;
                }
            }

            // 128-246
            // reserved for future use

            // 247
            public static class SameLocals1StackItemExtended extends StackMapFrame {
                private final int offsetDelta;
                private final VerificationTypeInfo[] stack;

                public SameLocals1StackItemExtended(int tag, int offsetDelta, VerificationTypeInfo[] stack) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                    this.stack = stack;
                }
            }

            // 248-250
            public static class Chop extends StackMapFrame {
                private final int offsetDelta;

                public Chop(int tag, int offsetDelta) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                }
            }

            // 251
            public static class SameFrameExtended extends StackMapFrame {
                private final int offsetDelta;

                public SameFrameExtended(int tag, int offsetDelta) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                }
            }

            // 252-254
            public static class Append extends StackMapFrame {
                private final int offsetDelta;
                private final VerificationTypeInfo[] locals;

                public Append(int tag, int offsetDelta, VerificationTypeInfo[] locals) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                    this.locals = locals;
                }
            }

            // 255
            public static class FullFrame extends StackMapFrame {
                private final int offsetDelta;
                private final VerificationTypeInfo[] locals;
                private final VerificationTypeInfo[] stack;

                public FullFrame(int tag, int offsetDelta, VerificationTypeInfo[] locals, VerificationTypeInfo[] stack) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                    this.locals = locals;
                    this.stack = stack;
                }
            }
        }

        public static class VerificationTypeInfo {
            private final int tag;

            public VerificationTypeInfo(int tag) {
                this.tag = tag;
            }
        }
    }

    public static class SourceFile extends AttributeInfo {
        private final int index;

        public SourceFile(int index) {
            super("SourceFile");
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
}
