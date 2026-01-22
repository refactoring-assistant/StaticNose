package edu.northeastern;

import edu.northeastern.cli.CommandLineInterface;
import picocli.CommandLine;

public class Main {
    static void main(String[] args) {
        CommandLineInterface app = new CommandLineInterface();

        int exitCode = new CommandLine(app).setCaseInsensitiveEnumValuesAllowed(true).execute(args);

        System.exit(exitCode);
    }
}
