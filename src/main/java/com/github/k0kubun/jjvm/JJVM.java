package com.github.k0kubun.jjvm;

import com.github.k0kubun.jjvm.classfile.ClassFileParser.DescriptorParser;
import com.github.k0kubun.jjvm.classfile.MethodInfo.Descriptor;
import com.github.k0kubun.jjvm.virtualmachine.Value;
import com.github.k0kubun.jjvm.virtualmachine.VirtualMachine;

// $ jjvm [class file]
public class JJVM {
    public static void main(String[] args) {
        JJVMOptions options = parseOptions(args);
        if (options == null || options.className == null) {
            printHelp();
            System.exit(1);
        }

        VirtualMachine vm = new VirtualMachine(options.getClassPath());
        Value.Class klass = vm.loadClass(options.getClassName());
        Descriptor mainMethod = DescriptorParser.parseMethod("([Ljava/lang/String;)V");
        vm.callStaticMethod(klass, "main", mainMethod, new Value[]{}); // TODO: pass rest args
    }

    private static void printHelp() {
        System.err.print(
                "Usage: jjvm [-options] class [args...]\n" +
                "where options include\n" +
                "    -cp <class search path of directories and zip/jar files>\n" +
                "    -classpath <class search path of directories and zip/jar files>\n" +
                "                  A : separated list of directories, JAR archives,\n" +
                "                  and ZIP archives to search for class files.\n"
        );
    }

    private static JJVMOptions parseOptions(String[] args) {
        String className = null;
        String classPath = ".";

        int i;
        for (i = 0; i < args.length; i++) {
            final String arg = args[i];
            if (!arg.startsWith("-")) {
                className = args[i];
                break;
            }

            if (arg.equals("-cp") || arg.equals("-classpath")) {
                i++;
                if (i == args.length) {
                    System.err.println("Error: " + arg + "requires class path specification");
                    return null;
                }
                classPath = args[i];
            } else {
                System.err.println("Unrecognized option: " + arg);
                System.exit(1);
            }
        }

        String[] rest = new String[args.length - i];
        if (rest.length > 0) {
            System.arraycopy(args, i, rest, 0, rest.length);
        }
        return new JJVMOptions(className, rest, classPath);
    }

    private static class JJVMOptions {
        private final String className;
        private final String[] args;
        private final String classPath;

        public JJVMOptions(String className, String[] args, String classPath) {
            this.className = className;
            this.args = args;
            this.classPath = classPath;
        }

        public String getClassName() {
            return className;
        }

        public String[] getArgs() {
            return args;
        }

        public String getClassPath() {
            return classPath;
        }
    }
}
