package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassLoader {
    private final List<String> jarPaths;

    public ClassLoader() {
        jarPaths = new ArrayList<>();
        setupBootstrapSearchPath();
    }

    public ClassFile loadClass(String klass) {
        String classPath = String.format("%s.class", klass);
        for (String jarPath : jarPaths) {
            try (ZipInputStream jarStream = new ZipInputStream(new FileInputStream(jarPath))) {
                ZipEntry entry;
                while ((entry = jarStream.getNextEntry()) != null) {
                    if (entry.getName().equals(classPath)) {
                        return new ClassFileParser().parse(jarStream);
                    }
                }
            } catch (FileNotFoundException e) {
                // expected for "jre/lib/sunrsasign.jar" and "jre/classes"
            } catch (IOException e) {
                System.out.println(String.format("Error while loading '%s': %s", jarPath, e.toString()));
            }
        }
        throw new RuntimeException(String.format("Class '%s' was not found in classpath", klass));
    }

    private void setupBootstrapSearchPath() {
        String bootclasspath = System.getProperty("sun.boot.class.path"); // java.class.path in Java 9+
        for (String jarPath : bootclasspath.split(":")) {
            jarPaths.add(jarPath);
        }
    }
}
