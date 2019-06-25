package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.FieldType;
import com.github.k0kubun.jjvm.classfile.MethodInfo;

class NativeMethod {
    static Value dispatch(Value.Class klass, MethodInfo method, Value[] args) {
        String className = klass.getClassFile().getThisClassName();

        // TODO: refactor

        if (className.equals("java/lang/System") && method.getName().equals("arraycopy")) {
            System.arraycopy(args[0].getValue(), (Integer) args[1].getValue(),
                    args[2].getValue(), (Integer) args[3].getValue(), (Integer) args[4].getValue());
            return null;
        } else if (className.equals("java/lang/System") && method.getName().equals("setIn0")) {
            klass.setField("in", args[0]);
            return null;
        } else if (className.equals("java/lang/System") && method.getName().equals("setOut0")) {
            klass.setField("out", args[0]);
            return null;
        } else if (className.equals("java/lang/System") && method.getName().equals("setErr0")) {
            klass.setField("err", args[0]);
            return null;
        } else if (className.equals("java/lang/Shutdown") && method.getName().equals("halt0")) {
            System.exit((int)args[0].getValue());
            return null;
        } else if (className.equals("java/lang/Float") && method.getName().equals("floatToRawIntBits")) {
            int result = Float.floatToRawIntBits((Float)args[0].getValue());
            return new Value(new FieldType.Int(), result);
        } else if (className.equals("java/lang/Double") && method.getName().equals("doubleToRawLongBits")) {
            long result = Double.doubleToRawLongBits((Double)args[0].getValue());
            return new Value(new FieldType.Long(), result);
        } else if (className.equals("java/lang/Double") && method.getName().equals("longBitsToDouble")) {
            double result = Double.longBitsToDouble((Long)args[0].getValue());
            return new Value(new FieldType.Double(), result);
        } else if (className.equals("java/lang/Object") && method.getName().equals("hashCode")) {
            return new Value(new FieldType.Int(), args[0].getValue().hashCode());
        }
        // === broken things from here ===
        else if (method.getName().equals("registerNatives")) {
            if (className.equals("java/lang/System")
                    || className.equals("java/lang/Object")
                    || className.equals("java/lang/Class")
                    || className.equals("java/lang/ClassLoader")
                    || className.equals("java/lang/Thread")
                    || className.equals("sun/misc/Unsafe")) {
                // nothing registered for now
            } else {
                throw new RuntimeException("Unsupported native method: " + klass.getClassFile().getThisClassName() + "." + method.getName());
            }
            return null;
        } else if (className.equals("java/lang/System") && method.getName().equals("initProperties")) {
            // not implemented properly yet. FIXME: implement something
            return Value.Null();
        } else if (className.equals("java/lang/Thread") && method.getName().equals("currentThread")) {
            // not implemented properly yet. FIXME: implement something
            return new Value(new FieldType.ObjectType("java/lang/Thread"), new Value.Object());
        } else if (className.equals("java/lang/Class") && method.getName().equals("desiredAssertionStatus0")) {
            // not implemented properly yet. FIXME: Is it okay?
            return new Value(new FieldType.Boolean(), true);
        } else if (className.equals("java/lang/Class") && method.getName().equals("getPrimitiveClass")) {
            // not implemented properly yet. FIXME: implement something
            return Value.Null();
        } else if ((className.equals("java/io/FileInputStream")
                || className.equals("java/io/FileOutputStream")
                || className.equals("java/io/FileDescriptor")) && method.getName().equals("initIDs")) {
            // not implemented properly yet. FIXME: what does it actually do?
            return null;
        } else if (className.equals("java/security/AccessController") && method.getName().equals("doPrivileged")) {
            // Super rough implementation for BufferedWriter.<init>. FIXME
            return new Value(new FieldType.ObjectType("java/lang/String"), new Value.Object(System.getProperty("line.separator")));
        } else if (className.equals("sun/misc/VM") && method.getName().equals("initialize")) {
            // not implemented properly yet. FIXME: implement something
            return null;
        } else {
            throw new RuntimeException("Unsupported native method: " + klass.getClassFile().getThisClassName() + "." + method.getName());
        }
    }
}
