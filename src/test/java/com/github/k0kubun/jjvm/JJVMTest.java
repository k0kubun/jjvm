package com.github.k0kubun.jjvm;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class JJVMTest {
    private static String BASE_PATH = "test/";

    @BeforeClass
    public static void setup() {
        CommandResult result = runCommand("./gradlew", "installDist");
        assertEquals(0, result.status);
    }

    @Test
    public void testHello() {
        testJJVM("Hello");
    }

    @Test
    public void testInt() {
        testJJVM("Int");
    }

    @Test
    public void testLong() {
        testJJVM("Long");
    }

    @Test
    public void testFloat() {
        testJJVM("Float");
    }

    @Test
    public void testDouble() {
        testJJVM("Double");
    }

    @Test
    public void testString() {
        testJJVM("Str");
    }

    private void testJJVM(String klass) {
        CommandResult result = runCommand("javac", BASE_PATH + klass + ".java");
        assertEquals(0, result.status);

        CommandResult java = runCommand("java", "-cp", BASE_PATH, klass);
        assertEquals(0, java.status);

        // TODO: Make jjvm interface compatible with java
        CommandResult jjvm = runCommand("build/install/jjvm/bin/jjvm", BASE_PATH + klass + ".class");
        assertEquals(0, jjvm.status);

        assertEquals(java.stdout, jjvm.stdout);
        assertEquals(java.stderr, jjvm.stderr);
    }

    private static CommandResult runCommand(String exec, String... args) {
        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add(exec);
        command.addAll(Arrays.asList(args));
        processBuilder.command(command);
        Process process;
        try {
            process = processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Failed to start process: " + e.toString());
        }

        BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        String outLine;
        String errLine = null;
        try {
            while ((outLine = outReader.readLine()) != null || (errLine = errReader.readLine()) != null) {
                if (outLine != null) {
                    stdout.append(outLine);
                }
                if (errLine != null) {
                    stderr.append(errLine);
                    errLine = null;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read output: " + e.toString());
        }

        int status;
        try {
            status = process.waitFor();
        } catch (InterruptedException e) {
            status = -1;
        }

        return new CommandResult(status, stdout.toString(), stderr.toString());
    }

    private static class CommandResult {
        int status;
        String stdout;
        String stderr;

        public CommandResult(int status, String stdout, String stderr) {
            this.status = status;
            this.stdout = stdout;
            this.stderr = stderr;
        }
    }
}
