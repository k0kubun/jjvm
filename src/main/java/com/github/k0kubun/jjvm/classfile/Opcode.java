package com.github.k0kubun.jjvm.classfile;

// https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-7.html
public enum Opcode {
    // === Constants ===
    // 00 (0x00)    nop
    // 01 (0x01)    aconst_null
    // 02 (0x02)    iconst_m1
    // 03 (0x03)    iconst_0
    // 04 (0x04)    iconst_1
    // 05 (0x05)    iconst_2
    // 06 (0x06)    iconst_3
    // 07 (0x07)    iconst_4
    // 08 (0x08)    iconst_5
    // 09 (0x09)    lconst_0
    // 10 (0x0a)    lconst_1
    // 11 (0x0b)    fconst_0
    // 12 (0x0c)    fconst_1
    // 13 (0x0d)    fconst_2
    // 14 (0x0e)    dconst_0
    // 15 (0x0f)    dconst_1
    // 16 (0x10)    bipush
    // 17 (0x11)    sipush
    // 18 (0x12)    ldc
    // 19 (0x13)    ldc_w
    // 20 (0x14)    ldc2_w
    Nop(0x00),
    AconstNull(0x01),
    IconstM1(0x02),
    Iconst0(0x03),
    Iconst1(0x04),
    //Iconst2(0x05),
    //Iconst3(0x06),
    //Iconst4(0x07),
    //Iconst5(0x08),
    Ldc(0x12),

    // === Loads ===
    // 21 (0x15)    iload
    // 22 (0x16)    lload
    // 23 (0x17)    fload
    // 24 (0x18)    dload
    // 25 (0x19)    aload
    // 26 (0x1a)    iload_0
    // 27 (0x1b)    iload_1
    // 28 (0x1c)    iload_2
    // 29 (0x1d)    iload_3
    // 30 (0x1e)    lload_0
    // 31 (0x1f)    lload_1
    // 32 (0x20)    lload_2
    // 33 (0x21)    lload_3
    // 34 (0x22)    fload_0
    // 35 (0x23)    fload_1
    // 36 (0x24)    fload_2
    // 37 (0x25)    fload_3
    // 38 (0x26)    dload_0
    // 39 (0x27)    dload_1
    // 40 (0x28)    dload_2
    // 41 (0x29)    dload_3
    // 42 (0x2a)    aload_0
    // 43 (0x2b)    aload_1
    // 44 (0x2c)    aload_2
    // 45 (0x2d)    aload_3
    // 46 (0x2e)    iaload
    // 47 (0x2f)    laload
    // 48 (0x30)    faload
    // 49 (0x31)    daload
    // 50 (0x32)    aaload
    // 51 (0x33)    baload
    // 52 (0x34)    caload
    // 53 (0x35)    saload
    Aload0(0x2a),

    // === Control ===
    // 167 (0xa7)    goto
    // 168 (0xa8)    jsr
    // 169 (0xa9)    ret
    // 170 (0xaa)    tableswitch
    // 171 (0xab)    lookupswitch
    // 172 (0xac)    ireturn
    // 173 (0xad)    lreturn
    // 174 (0xae)    freturn
    // 175 (0xaf)    dreturn
    // 176 (0xb0)    areturn
    // 177 (0xb1)    return
    Return(0xb1),

    // === References ===
    // 178 (0xb2)    getstatic
    // 179 (0xb3)    putstatic
    // 180 (0xb4)    getfield
    // 181 (0xb5)    putfield
    // 182 (0xb6)    invokevirtual
    // 183 (0xb7)    invokespecial
    // 184 (0xb8)    invokestatic
    // 185 (0xb9)    invokeinterface
    // 186 (0xba)    invokedynamic
    // 187 (0xbb)    new
    // 188 (0xbc)    newarray
    // 189 (0xbd)    anewarray
    // 190 (0xbe)    arraylength
    // 191 (0xbf)    athrow
    // 192 (0xc0)    checkcast
    // 193 (0xc1)    instanceof
    // 194 (0xc2)    monitorenter
    // 195 (0xc3)    monitorexit
    Getstatic(0xb2),
    Invokevirtual(0xb6),
    Invokespecial(0xb7);

    private final byte code;

    Opcode(int code) {
        this.code = (byte)code;
    }

    public byte getCode() {
        return code;
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
        throw new RuntimeException("Opcode is not defined for code: " + Integer.toHexString((int)code));
    }
}
