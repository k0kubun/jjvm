package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassLoader {
    private final Map<String, String> jarByClass;
    private final List<String> classPaths;

    public ClassLoader(String classPath) {
        jarByClass = new HashMap<>();
        setupBootstrapSearchPath(jarByClass);

        classPaths = new ArrayList<>();
        for (String path : classPath.split(":")) {
            classPaths.add(path);
        }
    }

    public ClassFile loadClass(String klass) {
        String filename = String.format("%s.class", klass);

        if (jarByClass.containsKey(filename)) {
            String jarPath = jarByClass.get(filename);
            try (ZipInputStream jarStream = new ZipInputStream(new FileInputStream(jarPath))) {
                ZipEntry entry;
                while ((entry = jarStream.getNextEntry()) != null) {
                    if (entry.getName().equals(filename)) {
                        return new ClassFileParser().parse(jarStream);
                    }
                }
            } catch (IOException e) {
                System.out.println(String.format("Error while loading '%s': %s", jarPath, e.toString()));
            }
            throw new RuntimeException(String.format("Failed to load '%s' from '%s'", filename, jarPath));
        }

        for (String classPath : classPaths) {
            String filepath = classPath + "/" + filename;
            if (new File(filepath).exists()) {
                try {
                    return new ClassFileParser().parse(filepath);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to open :" + e.getMessage());
                }
            }
        }

        throw new RuntimeException(String.format("Class '%s' was not found in classpath", klass));
    }

    private void setupBootstrapSearchPath(Map<String, String> jarByClass) {
        String bootclasspath = System.getProperty("sun.boot.class.path"); // java.class.path in Java 9+

        for (String jarPath : bootclasspath.split(":")) {
            try (ZipInputStream jarStream = new ZipInputStream(new FileInputStream(jarPath))) {
                ZipEntry entry;
                while ((entry = jarStream.getNextEntry()) != null) {
                    if (entry.getName().endsWith(".class")) {
                        if (jarByClass.containsKey(entry.getName())) {
                            throw new RuntimeException(String.format("'%s' exists in both '%s' and '%s'",
                                    entry.getName(), jarByClass.get(entry.getName()), jarPath));
                        }
                        jarByClass.put(entry.getName(), jarPath);
                    }
                }
            } catch (FileNotFoundException e) {
                // expected for "jre/lib/sunrsasign.jar" and "jre/classes"
            } catch (IOException e) {
                System.out.println(String.format("Error while loading '%s': %s", jarPath, e.toString()));
            }
        }
    }
}
