package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;

import java.util.ArrayList;
import java.util.List;

public class ClassLoader {
    private final List<String> jarPaths;

    public ClassLoader() {
        jarPaths = new ArrayList<>();
        setupBootstrapSearchPath();
    }

    public ClassFile loadClass(String klass) {
        for (String jarPath : jarPaths) {
            // TODO: open tar
        }
        return null;
    }

    private void setupBootstrapSearchPath() {
        String bootclasspath = System.getProperty("sun.boot.class.path"); // java.class.path in Java 9+
        for (String jarPath : bootclasspath.split(":")) {
            jarPaths.add(jarPath);
        }
    }
}
