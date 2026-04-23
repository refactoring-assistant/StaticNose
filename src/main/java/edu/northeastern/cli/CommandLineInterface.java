package edu.northeastern.cli;

import edu.northeastern.core.AnalysisGenerator;
import edu.northeastern.reporting.CSVReportGenerator;
import edu.northeastern.reporting.IReportGenerator;
import edu.northeastern.reporting.JSONReportGenerator;
import edu.northeastern.reporting.OracleEvaluator;
import edu.northeastern.reporting.ReportStruct;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ArgGroup;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "StaticNose", mixinStandardHelpOptions = true,  version = "StaticNose 0.1.0",
        description = "Detect code smells using Static Analysis.")
public class CommandLineInterface implements Callable<Integer> {

    // The folder is required universally for both modes
    @Option(names = {"-f", "--folder"}, converter = DirectoryValidator.class, required = true, description = "The folder to detect code smells in.")
    private File sourceFolder;

    // Mutually Exclusive Group: The user MUST choose exactly ONE of the nested modes
    @ArgGroup(multiplicity = "1")
    private ExecutionMode mode;

    static class ExecutionMode {
        @ArgGroup(exclusive = false, heading = "Analysis Options%n")
        AnalysisMode analysisMode;

        @ArgGroup(exclusive = false, heading = "Oracle Generation Options%n")
        OracleGenMode oracleGenMode;
    }

    // ALL options related to scanning for code smells live here
    static class AnalysisMode {
        @Option(names = {"-s", "--smell"}, split = ",", converter = CodeSmellConverter.class, required = true, description = "The code smells to detect. Separate with commas or use multiple -s flags. Valid values: ${COMPLETION-CANDIDATES}")
        private List<CodeSmell> codeSmells;

        @Option(names = {"-r", "--report-format"}, description = "Format of the report. (default: CSV)")
        private ReportFormat reportFormat = ReportFormat.CSV;

        @Option(names = {"-v", "--verbose"}, description = "Print metrics in report.")
        boolean verbose;

        @Option(names = {"-t", "--test-oracle"}, converter = FileValidator.class, description = "The test oracle file to compare the report against.")
        private File oracleFile;
    }

    // Options related purely to generating the CSV template live here
    static class OracleGenMode {
        @Option(names = {"-g", "--gen-oracle"}, required = true, description = "Generate a template test oracle file for the given.")
        boolean generateOracle;
    }

    @Override
    public Integer call() {

        // Check which mode the user selected
        boolean isOracleGenMode = mode != null && mode.oracleGenMode != null && mode.oracleGenMode.generateOracle;

        if(isOracleGenMode) {
            System.out.println("Generating oracle for: " + sourceFolder);
            edu.northeastern.reporting.OracleGenerator generator = new edu.northeastern.reporting.OracleGenerator(sourceFolder);

            // Start the generation process
            generator.generate();

        } else {
            // Because they aren't in GenMode, PicoCLI guarantees analysisMode is populated
            List<CodeSmell> codeSmells = mode.analysisMode.codeSmells;
            ReportFormat reportFormat = mode.analysisMode.reportFormat;
            File oracleFile = mode.analysisMode.oracleFile;
            boolean verbose = mode.analysisMode.verbose;

            System.out.println("Starting Analysis on: " + sourceFolder);
            System.out.println("Code Smells to detect: " + codeSmells);

            if (oracleFile != null) {
                System.out.println("Oracle file chosen: " + oracleFile.getAbsolutePath());
            }
            if (verbose) {
                System.out.println("Verbose mode is on");
            }

            // Start analysis
            IReportGenerator reportGenerator;

            if(reportFormat == ReportFormat.JSON) {
                reportGenerator = new JSONReportGenerator(sourceFolder.toString());
            } else {
                reportGenerator = new CSVReportGenerator(sourceFolder.toString());
            }

            AnalysisGenerator analysisGenerator = new AnalysisGenerator(sourceFolder, codeSmells, reportGenerator);

            // 1. Capture the generated reports from the analysis
            List<ReportStruct> generatedReports = analysisGenerator.start();

            // 2. Hook in the Oracle Evaluator if the -t flag was provided
            if (oracleFile != null) {
                System.out.println("\nStarting Oracle Evaluation...");
                OracleEvaluator evaluator = new OracleEvaluator(
                        oracleFile,
                        generatedReports,
                        codeSmells,
                        sourceFolder.toString()
                );
                evaluator.evaluateAndReport();
            }
        }
        return 0;
    }
}