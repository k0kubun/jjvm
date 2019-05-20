package com.github.k0kubun.jjvm.classfile;

public enum ConstantType {
    // Constant Type	         Value
    // CONSTANT_Class	             7
    // CONSTANT_Fieldref	         9
    // CONSTANT_Methodref	        10
    // CONSTANT_InterfaceMethodref	11
    // CONSTANT_String	             8
    // CONSTANT_Integer	             3
    // CONSTANT_Float	             4
    // CONSTANT_Long	             5
    // CONSTANT_Double	             6
    // CONSTANT_NameAndType    	    12
    // CONSTANT_Utf8	             1
    // CONSTANT_MethodHandle    	15
    // CONSTANT_MethodType	        16
    // CONSTANT_InvokeDynamic    	18
    Class(7),
    Fieldref(9),
    Methodref(10),
    InterfaceMethodref(11),
    String(8),
    Integer(3),
    Float(4),
    Long(5),
    Double(6),
    NameAndType(12),
    Utf8(1),
    MethodHandle(15),
    MethodType(16),
    InvokeDynamic(18);

    private final int tag;

    ConstantType(int tag) {
        this.tag = tag;
    }

    public int getTag() {
        return tag;
    }

    public static ConstantType fromTag(int tag) {
        for (ConstantType type : ConstantType.values()) {
            if (tag == type.getTag()) {
                return type;
            }
        }
        throw new RuntimeException("ConstantType is not defined for tag:" + new Integer(tag));
    }
}
