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
    public static void setup() throws IOException {
        CommandResult result = runCommand("./gradlew", "installDist");
        assertEquals(0, result.status);
    }

    @Test
    public void testHello() throws IOException {
        testJJVM("Hello");
    }

    private void testJJVM(String klass) throws IOException {
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

    private static CommandResult runCommand(String exec, String... args) throws IOException {
        ProcessBuilder processBuilder = new ProcessBuilder();

        List<String> command = new ArrayList<>();
        command.add(exec);
        command.addAll(Arrays.asList(args));
        processBuilder.command(command);
        Process process = processBuilder.start();

        BufferedReader outReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        StringBuilder stdout = new StringBuilder();
        StringBuilder stderr = new StringBuilder();
        String outLine;
        String errLine = null;
        while ((outLine = outReader.readLine()) != null || (errLine = errReader.readLine()) != null) {
            if (outLine != null) {
                stdout.append(outLine);
            }
            if (errLine != null) {
                stderr.append(errLine);
                errLine = null;
            }
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
