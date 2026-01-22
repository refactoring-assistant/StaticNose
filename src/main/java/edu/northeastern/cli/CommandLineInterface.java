package edu.northeastern.cli;

import edu.northeastern.core.AnalysisGenerator;
import edu.northeastern.reporting.CSVReportGenerator;
import edu.northeastern.reporting.IReportGenerator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.io.File;
import java.util.concurrent.Callable;

@Command(name = "ELASTIC", mixinStandardHelpOptions = true,  version = "ELASTIC 0.1.0",
description = "Detect code smells using Static Analysis.")
public class CommandLineInterface implements Callable<Integer> {

    @Option(names = {"-s", "--smell"}, converter = CodeSmellConverter.class, required = true, description = "The code smell to detect. Valid values: ${COMPLETION-CANDIDATES}")
    private CodeSmell codeSmell;

    @Option(names = {"-f", "--folder"}, converter = DirectoryValidator.class, required = true, description = "The folder to detect code smells in.")
    private File sourceFolder;

    @Option(names = {"-r", "--report-format"}, description = "Format of the report. (default: CSV)")
    private ReportFormat reportFormat = ReportFormat.CSV;

    @ArgGroup
    private OperationMode mode;

    static class AnalysisOptions {
        @Option(names = {"-v", "--verbose"}, description = "Print metrics in report.")
        boolean verbose;

        @Option(names = {"-t", "--test-oracle"}, converter = FileValidator.class, description = "The test oracle file to compare the report against.")
        private File oracleFile;
    }

    static class OracleGenerationOptions {
        @Option(names = {"-g", "--gen-oracle"}, required = true, description = "Generate a template test oracle file for the given.")
        boolean generateOracle;
    }

    static class OperationMode {
        @ArgGroup(exclusive=false, heading = "Analysis Options%n")
        AnalysisOptions analysisOptions;

        @ArgGroup(exclusive=false, heading = "Oracle Generation Options%n")
        OracleGenerationOptions oracleGenerationOptions;
    }

    @Override
    public Integer call() throws Exception {

        boolean isOracleGenMode = mode != null
                && mode.oracleGenerationOptions != null
                && mode.oracleGenerationOptions.generateOracle;

        if(isOracleGenMode) {
            System.out.println("Generating oracle for: " + sourceFolder);
            System.out.println("Code Smell to generate oracle for: " + codeSmell);

            // Generate oracle
            // 1. Create oracle generator object with sourceFolder, codeSmell
            //    and reportFormat and then start(). It will do everything
        } else {

            File oracleFile = (mode != null && mode.analysisOptions != null) ? mode.analysisOptions.oracleFile : null;
            boolean verbose = (mode != null && mode.analysisOptions != null) && mode.analysisOptions.verbose;

            System.out.println("Starting Analysis on: " + sourceFolder);
            System.out.println("Code Smell to detect: " + codeSmell);

            if (oracleFile != null) {
                System.out.println("Oracle file chosen: " + oracleFile);
            }
            if (verbose) {
                System.out.println("Verbose mode is on");
            }

            // Start analysis
            // 1. Create the ReportGenerator object with the requested format type


            IReportGenerator CsvReportGenerator = new CSVReportGenerator();


            // 2. Create the AnalysisGenerator object with the params
            AnalysisGenerator analysisGenerator = new AnalysisGenerator(sourceFolder, codeSmell, CsvReportGenerator);
            analysisGenerator.start();

            //    analysisgen will automatically call the report generator object so no worries
            // 3. program end?

        }
        return 0;
    }

}
