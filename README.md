# JJVM [![wercker status](https://app.wercker.com/status/0582d36721f6ab149b9a34d5e3841bae/s/master "wercker status")](https://app.wercker.com/project/byKey/0582d36721f6ab149b9a34d5e3841bae)

JJVM is a _toy_ JVM implementation which is written in Java, aiming for self-hosting.

## Project Status

Early stage of development.

* `-help` can be self-hosted, but self-hosting of interpretation is WIP.
* Its boot is so slow for running `System.initializeSystemClass()` call.
* Not tested with any real-world application. Yours won't work either. See also: [test/](./test)

## Implementation scope

* Bytecode interpreter
* Native method
  * ... in Java :thinking:
* Reading rt.jar of host JVM
* Java classfile parser
  * Not relying on `com.sun.tools.classfile.*` :wink:

## Demo

```console
$ java -cp build/classes/java/main com.github.k0kubun.jjvm.JJVM -help
Usage: jjvm [-options] class [args...]
where options include
    -cp <class search path of directories and zip/jar files>
    -classpath <class search path of directories and zip/jar files>
                  A : separated list of directories, JAR archives,
                  and ZIP archives to search for class files.
    -help         print this help message

$ java -cp build/classes/java/main com.github.k0kubun.jjvm.JJVM \
>      -cp build/classes/java/main com.github.k0kubun.jjvm.JJVM -help
Usage: jjvm [-options] class [args...]
where options include
    -cp <class search path of directories and zip/jar files>
    -classpath <class search path of directories and zip/jar files>
                  A : separated list of directories, JAR archives,
                  and ZIP archives to search for class files.
    -help         print this help message

$ java -cp build/classes/java/main com.github.k0kubun.jjvm.JJVM -Xjjvmtrace \
>      -cp build/classes/java/main com.github.k0kubun.jjvm.JJVM -help
java/lang/System.<clinit>
  java/lang/System.registerNatives
java/lang/Class.<clinit>
  java/lang/Class.registerNatives
java/lang/System.initializeSystemClass
  ...(snip)...
  java/lang/System.newPrintStream
    java/io/BufferedOutputStream.<init>
      java/io/FilterOutputStream.<init>
        java/io/OutputStream.<init>
          java/lang/Object.<init>
    java/io/PrintStream.<init>
      ..(snip)...
  java/lang/System.setErr0
  ...(snip)...
com/github/k0kubun/jjvm/JJVM.main
  com/github/k0kubun/jjvm/JJVM.parseOptions
    ...(snip)...
    com/github/k0kubun/jjvm/JJVM.printHelp
      java/io/PrintStream.print
        java/io/PrintStream.write
          java/io/PrintStream.ensureOpen
          java/io/Writer.write
            java/lang/String.length
            java/io/BufferedWriter.write
              java/io/BufferedWriter.ensureOpen
              java/io/BufferedWriter.min
              java/lang/String.getChars
                java/lang/System.arraycopy
          java/io/BufferedWriter.flushBuffer
            java/io/BufferedWriter.ensureOpen
            java/io/OutputStreamWriter.write
              sun/nio/cs/StreamEncoder.write
                sun/nio/cs/StreamEncoder.ensureOpen
                sun/nio/cs/StreamEncoder.implWrite
Usage: jjvm [-options] class [args...]
where options include
    -cp <class search path of directories and zip/jar files>
    -classpath <class search path of directories and zip/jar files>
                  A : separated list of directories, JAR archives,
                  and ZIP archives to search for class files.
    -help         print this help message
                ...(snip)...
    java/lang/System.exit
      ...(snip)...
      java/lang/Runtime.getRuntime
      java/lang/Runtime.exit
        ...(snip)...
        java/lang/Shutdown.exit
          java/lang/Shutdown.sequence
            java/lang/Shutdown.runHooks
          java/lang/Shutdown.halt
            java/lang/Shutdown.halt0
```

You may notice the help output seems not buffered.  
`Charset` is not working and we ended up stubbing `sun/nio/cs/StreamEncoder.implWrite` for now.

Complete call tree: https://gist.github.com/k0kubun/8a0b27ae48a5239c8b25af804e8d251f

## How to run

JDK 8 should be used. Others are not tested.

### VM

```bash
./gradlew jjvm -Pargs="-cp test Hello"
```

### Disassemble

```bash
./gradlew jjvmp -Pargs="test/Hello.class"
```

## Implementation overview

* `JJVM.main`: [com/github/k0kubun/jjvm/JJVM.java](./src/main/java/com/github/k0kubun/jjvm/JJVM.java)
  * `ClossLoader.setupBootstrapSearchPath`: [com/github/k0kubun/jjvm/virtualmachine/ClassLoader.java](./src/main/java/com/github/k0kubun/jjvm/virtualmachine/ClassLoader.java)
  * `ClassFileParser.parse`: [com/github/k0kubun/jjvm/classfile/ClassFileParser.java](./src/main/java/com/github/k0kubun/jjvm/classfile/ClassFileParser.java)
  * `VirtualMachine.executeMethod`: [com/github/k0kubun/jjvm/virtualmachine/VirtualMachine.java](./src/main/java/com/github/k0kubun/jjvm/virtualmachine/VirtualMachine.java)
    * `BytecodeInterpreter.execute`: [com/github/k0kubun/jjvm/virtualmachine/BytecodeInterpreter.java](./src/main/java/com/github/k0kubun/jjvm/virtualmachine/BytecodeInterpreter.java)
    * `NativeMethod.dispatch`: [com/github/k0kubun/jjvm/virtualmachine/NativeMethod.java](./src/main/java/com/github/k0kubun/jjvm/virtualmachine/NativeMethod.java)

## JDK References

* [Chapter 2. The Structure of the Java Virtual Machine](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-2.html)
* [Chapter 4. The class File Format](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-4.html)
* [Chapter 6. The Java Virtual Machine Instruction Set](https://docs.oracle.com/javase/specs/jvms/se8/html/jvms-6.html)

## License

MIT License
