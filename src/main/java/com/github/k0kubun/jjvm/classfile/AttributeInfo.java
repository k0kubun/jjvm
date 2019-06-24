package com.github.k0kubun.jjvm.classfile;

public class AttributeInfo {
    private final String name;

    AttributeInfo(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static class Code extends AttributeInfo {
        private final int maxStack;
        private final int maxLocals;
        private final Instruction[] instructions;
        private final ExceptionTableEntry[] exceptionTable;
        private final AttributeInfo[] attributes;

        Code(int maxStack, int maxLocals, Instruction[] instructions, ExceptionTableEntry[] exceptionTable, AttributeInfo[] attributes) {
            super("Code");
            this.maxStack = maxStack;
            this.maxLocals = maxLocals;
            this.instructions = instructions;
            this.exceptionTable = exceptionTable;
            this.attributes = attributes;
        }

        public Instruction[] getInstructions() {
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

            ExceptionTableEntry(int startPc, int endPc, int handlerPc, int catchType) {
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

        LineNumberTable(LineNumberEntry[] lineNumberTable) {
            super("LineNumberTable");
            this.lineNumberTable = lineNumberTable;
        }

        public LineNumberEntry[] getLineNumberTable() {
            return lineNumberTable;
        }

        public static class LineNumberEntry {
            private final int startPc;
            private final int lineNumber;

            LineNumberEntry(int startPc, int lineNumber) {
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

        StackMapTable(StackMapFrame[] entries) {
            super("StackMapTable");
            this.entries = entries;
        }

        public static class StackMapFrame {
            private final int tag;

            StackMapFrame(int tag) {
                this.tag = tag;
            }

            // 0-63
            public static class Same extends StackMapFrame {
                Same(int tag) {
                    super(tag);
                }
            }

            // 64-127
            public static class SameLocals1StackItem extends StackMapFrame {
                private final VerificationTypeInfo[] stack;

                SameLocals1StackItem(int tag, VerificationTypeInfo[] stack) {
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

                SameLocals1StackItemExtended(int tag, int offsetDelta, VerificationTypeInfo[] stack) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                    this.stack = stack;
                }
            }

            // 248-250
            public static class Chop extends StackMapFrame {
                private final int offsetDelta;

                Chop(int tag, int offsetDelta) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                }
            }

            // 251
            public static class SameFrameExtended extends StackMapFrame {
                private final int offsetDelta;

                SameFrameExtended(int tag, int offsetDelta) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                }
            }

            // 252-254
            public static class Append extends StackMapFrame {
                private final int offsetDelta;
                private final VerificationTypeInfo[] locals;

                Append(int tag, int offsetDelta, VerificationTypeInfo[] locals) {
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

                FullFrame(int tag, int offsetDelta, VerificationTypeInfo[] locals, VerificationTypeInfo[] stack) {
                    super(tag);
                    this.offsetDelta = offsetDelta;
                    this.locals = locals;
                    this.stack = stack;
                }
            }
        }

        public static class VerificationTypeInfo {
            private final int tag;

            VerificationTypeInfo(int tag) {
                this.tag = tag;
            }
        }
    }

    public static class SourceFile extends AttributeInfo {
        private final int index;

        SourceFile(int index) {
            super("SourceFile");
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    public static class ConstantValue extends AttributeInfo {
        private final ConstantInfo constantValue;

        ConstantValue(ConstantInfo constantValue) {
            super("ConstantValue");
            this.constantValue = constantValue;
        }

        public ConstantInfo getConstantValue() {
            return constantValue;
        }
    }
}
