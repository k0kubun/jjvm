package com.github.k0kubun.jjvm.classfile;

public class Instruction {
    private final Opcode opcode;
    private final byte[] operands;

    public Instruction(Opcode opcode, byte[] operands) {
        this.opcode = opcode;
        this.operands = operands;
    }

    public Opcode getOpcode() {
        return opcode;
    }

    public byte[] getOperands() {
        return operands;
    }

    // "indexbyte" operand
    public int getIndex() {
        return ((int)this.operands[0] << 4) + (int)this.operands[1];
    }

    // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html
    // https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-7.html
    public enum Opcode {
        // === Constants ===
        Nop(0x00, 0),
        Aconst_Null(0x01, 0),
        Iconst_M1(0x02, 0),
        Iconst_0(0x03, 0),
        Iconst_1(0x04, 0),
        Iconst_2(0x05, 0),
        Iconst_3(0x06, 0),
        Iconst_4(0x07, 0),
        Iconst_5(0x08, 0),
        Lconst_0(0x09, 0),
        Lconst_1(0x0a, 0),
        Fconst_0(0x0b, 0),
        Fconst_1(0x0c, 0),
        Fconst_2(0x0d, 0),
        //Dconst_0(0x0e, 0),
        //Dconst_1(0x0f, 0),
        Bipush(0x10, 1), // byte
        Sipush(0x11, 2), // byte1, byte2
        Ldc(0x12, 1), // index
        //Ldc_W(0x13, 2), // indexbyte1, indexbyte2
        Ldc2_W(0x14, 2), // indexbyte1, indexbyte2

        // === Loads ===
        Iload(0x15, 1), // index
        Lload(0x16, 1), // index
        Fload(0x17, 1), // index
        //Dload(0x18, 1), // index
        Aload(0x19, 1), // index
        Iload_0(0x1a, 0),
        Iload_1(0x1b, 0),
        Iload_2(0x1c, 0),
        Iload_3(0x1d, 0),
        Lload_0(0x1e, 0),
        Lload_1(0x1f, 0),
        Lload_2(0x20, 0),
        Lload_3(0x21, 0),
        Fload_0(0x22, 0),
        Fload_1(0x23, 0),
        Fload_2(0x24, 0),
        Fload_3(0x25, 0),
        //Dload_0(0x26, 0),
        Dload_1(0x27, 0),
        Dload_2(0x28, 0),
        //Dload_3(0x29, 0),
        Aload_0(0x2a, 0),
        Aload_1(0x2b, 0),
        Aload_2(0x2c, 0),
        Aload_3(0x2d, 0),
        //Iaload(0x2e, ),
        //Laload(0x2f, ),
        //Faload(0x30, ),
        //Daload(0x31, ),
        //Aaload(0x32, ),
        //Baload(0x33, ),
        Caload(0x34, 0),
        //Saload(0x35, ),

        // === Stores ===
        Istore(0x36, 1), // index
        Lstore(0x37, 1), // index
        Fstore(0x38, 1), // index
        //Dstore(0x39, 1), // index
        Astore(0x3a, 1), // index
        Istore_0(0x3b, 0),
        Istore_1(0x3c, 0),
        Istore_2(0x3d, 0),
        Istore_3(0x3e, 0),
        Lstore_0(0x3f, 0),
        Lstore_1(0x40, 0),
        Lstore_2(0x41, 0),
        Lstore_3(0x42, 0),
        Fstore_0(0x43, 0),
        Fstore_1(0x44, 0),
        Fstore_2(0x45, 0),
        Fstore_3(0x46, 0),
        //Dstore_0(0x47, 0),
        //Dstore_1(0x48, 0),
        //Dstore_2(0x49, 0),
        //Dstore_3(0x4a, 0),
        Astore_0(0x4b, 0),
        Astore_1(0x4c, 0),
        Astore_2(0x4d, 0),
        Astore_3(0x4e, 0),
        //Iastore(0x4f, ),
        //Lastore(0x50, ),
        //Fastore(0x51, ),
        //Dastore(0x52, ),
        //Aastore(0x53, ),
        //Bastore(0x54, ),
        //Castore(0x55, ),
        //Sastore(0x56, ),

        // === Stack ===
        Pop(0x57, 0),
        //Pop2(0x58, 0),
        Dup(0x59, 0),
        //Dup_X1(0x5a, ),
        //Dup_X2(0x5b, ),
        //Dup2(0x5c, ),
        //Dup2_X1(0x5d, ),
        //Dup2_X2(0x5e, ),
        //Swap(0x5f, ),

        // === Math ===
        Iadd(0x60, 0),
        Ladd(0x61, 0),
        Fadd(0x62, 0),
        //Dadd(0x63, 0),
        Isub(0x64, 0),
        Lsub(0x65, 0),
        Fsub(0x66, 0),
        //Dsub(0x67, 0),
        Imul(0x68, 0),
        Lmul(0x69, 0),
        Fmul(0x6a, 0),
        //Dmul(0x6b, 0),
        Idiv(0x6c, 0),
        Ldiv(0x6d, 0),
        Fdiv(0x6e, 0),
        //Ddiv(0x6f, 0),
        Irem(0x70, 0),
        Lrem(0x71, 0),
        Frem(0x72, 0),
        //Drem(0x73, 0),
        Ineg(0x74, 0),
        Lneg(0x75, 0),
        Fneg(0x76, 0),
        //Dneg(0x77, 0),
        //Ishl(0x78, ),
        //Lshl(0x79, ),
        //Ishr(0x7a, ),
        //Lshr(0x7b, ),
        //Iushr(0x7c, ),
        //Lushr(0x7d, ),
        //Iand(0x7e, ),
        //Land(0x7f, ),
        Ior(0x80, 0),
        //Lor(0x81, ),
        //Ixor(0x82, ),
        //Lxor(0x83, ),
        Iinc(0x84, 2), // index, const

        // === Comparisons ===
        //Lcmp(0x94, ),
        //Fcmpl(0x95, ),
        //Fcmpg(0x96, ),
        //Dcmpl(0x97, ),
        //Dcmpg(0x98, ),
        Ifeq(0x99, 2), // branchbyte1, branchbyte2
        Ifne(0x9a, 2), // branchbyte1, branchbyte2
        Iflt(0x9b, 2), // branchbyte1, branchbyte2
        //Ifge(0x9c, ),
        //Ifgt(0x9d, ),
        //Ifle(0x9e, ),
        //IfIcmpeq(0x9f, ),
        IfIcmpeq(0xa0, 2), // branchbyte1, branchbyte2
        //IfIcmplt(0xa1, ),
        IfIcmpge(0xa2, 2), // branchbyte1, branchbyte2
        //IfIcmpgt(0xa3, ),
        //IfIcmple(0xa4, ),
        IfAcmpeq(0xa5, 2), // branchbyte1, branchbyte2
        //IfAcmpne(0xa6, ),

        // === Control ===
        Goto(0xa7, 2), // branchbyte1, branchbyte2
        //Jsr(0xa8, ),
        //Ret(0xa9, ),
        //Tableswitch(0xaa, ),
        //Lookupswitch(0xab, ),
        Ireturn(0xac, 0),
        Lreturn(0xad, 0),
        Freturn(0xae, 0),
        //Dreturn(0xaf, 0),
        Areturn(0xb0, 0),
        Return(0xb1, 0),

        // === References ===
        Getstatic(0xb2, 2), // indexbyte1, indexbyte2
        Putstatic(0xb3, 2), // indexbyte1, indexbyte2
        Getfield(0xb4, 2), // indexbyte1, indexbyte2
        Putfield(0xb5, 2), // indexbyte1, indexbyte2
        Invokevirtual(0xb6, 2), // indexbyte1, indexbyte2
        Invokespecial(0xb7, 2), // indexbyte1, indexbyte2
        Invokestatic(0xb8, 2), // indexbyte1, indexbyte2
        Invokeinterface(0xb9, 4), // indexbyte1, indexbyte2, count, 0
        //Invokedynamic(0xba, ),
        New(0xbb, 2), // indexbyte1, indexbyte2
        //Newarray(0xbc, ),
        //Anewarray(0xbd, ),
        Arraylength(0xbe, 0),
        Athrow(0xbf, 0),
        Checkcast(0xc0, 2), // indexbyte1, indexbyte2
        Instanceof(0xc1, 0), // indexbyte1, indexbyte2
        Monitorenter(0xc2, 0),
        Monitorexit(0xc3, 0),

        // === Extended ===
        //Wide(0xc4, ),
        //Multianewarray(0xc5, ),
        Ifnull(0xc6, 2), // branchbyte1, branchbyte2
        Ifnonnull(0xc7, 2); // branchbyte1, branchbyte2
        //Goto_W(0xc8, ),
        //Jsr_W(0xc9, ),

        private final byte code;
        private final int argc;

        Opcode(int code, int argc) {
            this.code = (byte)code;
            this.argc = argc;
        }

        public byte getCode() {
            return code;
        }

        public int getArgc() {
            return argc;
        }

        public String getName() {
            return toString().replaceAll("([a-z\\d])([A-Z])", "$1_$2").toLowerCase();
        }

        public static Opcode fromCode(byte code) {
            for (Opcode opcode : Opcode.values()) {
                if (opcode.getCode() == code) {
                    return opcode;
                }
            }
            throw new RuntimeException(String.format("Opcode is not defined for code: 0x%02x", code));
        }
    }
}
