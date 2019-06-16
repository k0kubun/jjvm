package com.github.k0kubun.jjvm.virtualmachine;

import com.github.k0kubun.jjvm.classfile.ClassFile;
import com.github.k0kubun.jjvm.classfile.ClassFileParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ClassLoader {
    private final List<String> bootPaths;
    private final List<String> classPaths;

    public ClassLoader(String classPath) {
        bootPaths = new ArrayList<>();
        setupBootstrapSearchPath(bootPaths);

        classPaths = new ArrayList<>();
        for (String path : classPath.split(":")) {
            classPaths.add(path);
        }
    }

    public ClassFile loadClass(String klass) {
        String filename = String.format("%s.class", klass);

        for (String bootPath : bootPaths) {
            try (ZipInputStream jarStream = new ZipInputStream(new FileInputStream(bootPath))) {
                ZipEntry entry;
                while ((entry = jarStream.getNextEntry()) != null) {
                    if (entry.getName().equals(filename)) {
                        return new ClassFileParser().parse(jarStream);
                    }
                }
            } catch (FileNotFoundException e) {
                // expected for "jre/lib/sunrsasign.jar" and "jre/classes"
            } catch (IOException e) {
                System.out.println(String.format("Error while loading '%s': %s", bootPath, e.toString()));
            }
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

    private void setupBootstrapSearchPath(List<String> paths) {
        String bootclasspath = System.getProperty("sun.boot.class.path"); // java.class.path in Java 9+
        for (String path : bootclasspath.split(":")) {
            paths.add(path);
        }
    }
}
